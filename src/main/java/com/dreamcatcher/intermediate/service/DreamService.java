package com.dreamcatcher.intermediate.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dreamcatcher.intermediate.model.Dream;
import com.dreamcatcher.intermediate.model.DreamWithoutImage;
import com.dreamcatcher.intermediate.model.User;
import com.dreamcatcher.intermediate.repository.DreamRepository;
import com.dreamcatcher.intermediate.repository.UserRepository;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class DreamService {

    @Autowired
    private DreamRepository dreamRepo;
    @Autowired
    private UserRepository userRepo;

    private OkHttpClient stableHordeClient = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final String API_KEY = "sTIrn203X2lialOLQrx53A";
    private static final String BASE_URL = "https://stablehorde.net/api/v2";
    private static final String ASYNC_ENDPOINT  = BASE_URL + "/generate/async";
    private static final String CHECK_ENDPOINT  = BASE_URL + "/generate/check";
    private static final String STATUS_ENDPOINT = BASE_URL + "/generate/status";

    /**
     * Création du rêve : on enregistre direct dans la BDD 
     * avec imageData = null, jobId = id du job stable horde (si on l'a).
     */
    public Dream saveDream(String content, String alias) {
        Optional<User> possible = userRepo.findByUsername(alias);
        if (possible.isEmpty()) {
            throw new IllegalArgumentException("No user found");
        }
        User realUser = possible.get();
        Dream d = new Dream();
        d.setContent(content);
        d.setUser(realUser);
        d.setCreatedAt(LocalDateTime.now());
        d.setImageData(null);
        
        // On lance la génération asynchrone (pas de polling). 
        // Si on veut ignorer l'erreur, on ne jette pas d'exception, 
        // on stocke juste jobId = null si ça rate.
        String jobId = submitAsyncJob(content);
        d.setJobId(jobId);

        return dreamRepo.save(d);
    }

    public List<Dream> getAllDreams() {
        // On récupère tous les rêves 
        List<Dream> all = dreamRepo.findAll();
        // On tente de mettre à jour l'image pour ceux qui n'en ont pas 
        // mais qui ont un jobId
        for (Dream d : all) {
            tryUpdatingImageIfReady(d);
        }
        return all;
    }

    public List<DreamWithoutImage> searchDreams(String q) {
        // Pareil pour la recherche mais sans l'image
        List<DreamWithoutImage> matches = dreamRepo.findByPartialContent(q);
        return matches;
    }

    public Optional<Dream> getDreamById(Long id) {
        Optional<Dream> found = dreamRepo.findById(id);
        found.ifPresent(this::tryUpdatingImageIfReady);
        return found;
    }

    public boolean deleteDream(Long id, String alias) {
        Optional<Dream> found = dreamRepo.findById(id);
        if (found.isPresent()) {
            Dream x = found.get();
            if (x.getUser().getUsername().equals(alias)) {
                dreamRepo.delete(x);
                return true;
            } else {
                throw new IllegalArgumentException("Unauthorized");
            }
        }
        return false;
    }
    
    /**
     * Soumet juste le prompt à /generate/async -> renvoie jobId ou null si échec
     */
    private String submitAsyncJob(String prompt) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("prompt", prompt);
            RequestBody body = RequestBody.create(
                    payload.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );
            Request req = new Request.Builder()
                .url(ASYNC_ENDPOINT)
                .header("apikey", API_KEY)
                .post(body)
                .build();
            Response resp = stableHordeClient.newCall(req).execute();
            if (!resp.isSuccessful()) {
                return null;
            }
            String raw = resp.body().string();
            JSONObject obj = new JSONObject(raw);
            // stablehorde renvoie "id"
            if (obj.has("id")) {
                return obj.getString("id");
            }
        } catch (IOException | JSONException e) {
            return null;
        }
        return null;
    }

    /**
     * Appelé à chaque GET de rêve(s). 
     *  - Si imageData != null => on ne fait rien (déjà prêt).
     *  - Si jobId == null => on ne fait rien (pas d'IA demandée).
     *  - Sinon, on check si done == true. 
     *    - Si oui, on fetch /generate/status pour avoir le lien, 
     *      on télécharge l'image, on update la BDD
     *    - Sinon, rien de plus, imageData reste null pour l'instant.
     */
    private void tryUpdatingImageIfReady(Dream d) {
        if (d.getImageData() != null) {
            return; // Déjà mise à jour
        }
        if (d.getJobId() == null) {
            return; // Pas de job
        }
        // Check /generate/check/{jobId}
        boolean isDone = isJobDone(d.getJobId());
        if (!isDone) {
            return; // pas encore fini
        }
        // Si c'est fini, on récupère l'URL
        String finalUrl = fetchFinalUrl(d.getJobId());
        if (finalUrl == null) {
            return;
        }
        // On télécharge l'image
        byte[] pic = downloadImage(finalUrl);
        if (pic != null && pic.length > 0) {
            d.setImageData(pic);
            dreamRepo.save(d); // update la BDD
        }
    }

    private boolean isJobDone(String jobId) {
        String checkUrl = CHECK_ENDPOINT + "/" + jobId;
        Request r = new Request.Builder()
            .url(checkUrl)
            .header("apikey", API_KEY)
            .get()
            .build();
        try (Response resp = stableHordeClient.newCall(r).execute()) {
            if (!resp.isSuccessful()) {
                return false;
            }
            String raw = resp.body().string();
            JSONObject obj = new JSONObject(raw);
            return obj.optBoolean("done", false);
        } catch (IOException | JSONException e) {
            return false;
        }
    }

    private String fetchFinalUrl(String jobId) {
        String url = STATUS_ENDPOINT + "/" + jobId;
        Request r = new Request.Builder()
            .url(url)
            .header("apikey", API_KEY)
            .get()
            .build();
        try (Response resp = stableHordeClient.newCall(r).execute()) {
            if (!resp.isSuccessful()) {
                return null;
            }
            String raw = resp.body().string();
            JSONObject obj = new JSONObject(raw);
            if (!obj.optBoolean("done", false)) {
                return null;
            }
            JSONArray arr = obj.optJSONArray("generations");
            if (arr != null && arr.length() > 0) {
                JSONObject first = arr.getJSONObject(0);
                return first.optString("img", null);
            }
        } catch (IOException | JSONException e) {
            return null;
        }
        return null;
    }

    private byte[] downloadImage(String link) {
        Request req = new Request.Builder()
            .url(link)
            .get()
            .build();
        try (Response resp = stableHordeClient.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                return null;
            }
            return resp.body().bytes();
        } catch (IOException e) {
            return null;
        }
    }
}
