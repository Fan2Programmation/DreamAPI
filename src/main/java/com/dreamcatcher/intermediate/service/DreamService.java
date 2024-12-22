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

    private OkHttpClient stableHordeClient = new OkHttpClient();

    private static final String API_KEY = "sTIrn203X2lialOLQrx53A";
    private static final String BASE_URL = "https://stablehorde.net/api/v2";
    private static final String ASYNC_ENDPOINT  = BASE_URL + "/generate/async";
    private static final String CHECK_ENDPOINT  = BASE_URL + "/generate/check";
    private static final String STATUS_ENDPOINT = BASE_URL + "/generate/status";

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

        // Récupération de l'image en binaire
        byte[] pictureData = generateImage(content);
        d.setImageData(pictureData);

        return dreamRepo.save(d);
    }

    public List<Dream> getAllDreams() {
        return dreamRepo.findAll();
    }

    public List<Dream> searchDreams(String q) {
        return dreamRepo.findByContentContainingIgnoreCase(q);
    }

    public Optional<Dream> getDreamById(Long id) {
        return dreamRepo.findById(id);
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
     * 1) POST vers /generate/async -> on récupère un jobId
     * 2) Poll /generate/check/{jobId} jusqu’à done == true
     * 3) GET sur /generate/status/{jobId} pour récupérer l’URL "img"
     * 4) GET sur cette URL -> bytes -> on stocke
     */
    private byte[] generateImage(String prompt) {
        String jobId = submitAsyncJob(prompt);
        if (jobId == null) {
            return null;
        }
        boolean done = waitUntilDone(jobId);
        if (!done) {
            return null;
        }
        String imageLink = fetchFinalUrl(jobId);
        if (imageLink == null) {
            return null;
        }
        return downloadImage(imageLink);
    }

    /**
     * Étape 1 : POST vers /generate/async
     * Retourne jobId ou null si échec
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
            // Selon la doc, on récupère "id"
            if (obj.has("id")) {
                return obj.getString("id");
            }
        } catch (IOException | JSONException e) {
            return null;
        }
        return null;
    }

    /**
     * Étape 2 : Poll /generate/check/{jobId} jusqu’à "done" == true
     * On tente X fois toutes les 5 secondes.
     */
    private boolean waitUntilDone(String jobId) {
        for (int i = 0; i < 10; i++) {
            try {
                TimeUnit.SECONDS.sleep(5);
                String checkUrl = CHECK_ENDPOINT + "/" + jobId;
                Request r = new Request.Builder()
                        .url(checkUrl)
                        .header("apikey", API_KEY)
                        .get()
                        .build();
                Response resp = stableHordeClient.newCall(r).execute();
                if (!resp.isSuccessful()) {
                    continue;
                }
                String raw = resp.body().string();
                JSONObject obj = new JSONObject(raw);
                // "done": true/false
                boolean finished = obj.optBoolean("done", false);
                if (finished) {
                    return true;
                }
            } catch (IOException | JSONException e) {
                // On ignore et on retente
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Étape 3 : GET /generate/status/{jobId} pour obtenir l’URL "img" 
     * (selon la doc, c’est "generations"[0].img).
     */
    private String fetchFinalUrl(String jobId) {
        try {
            String statusUrl = STATUS_ENDPOINT + "/" + jobId;
            Request r = new Request.Builder()
                    .url(statusUrl)
                    .header("apikey", API_KEY)
                    .get()
                    .build();
            Response resp = stableHordeClient.newCall(r).execute();
            if (!resp.isSuccessful()) {
                return null;
            }
            String raw = resp.body().string();
            JSONObject obj = new JSONObject(raw);
            if (!obj.optBoolean("done", false)) {
                return null; // la doc indique "done" == true si terminé
            }
            JSONArray gens = obj.optJSONArray("generations");
            if (gens != null && gens.length() > 0) {
                JSONObject first = gens.getJSONObject(0);
                return first.optString("img", null);
            }
        } catch (IOException | JSONException e) {
            return null;
        }
        return null;
    }

    /**
     * Étape 4 : On récupère l’image (webp, png, etc.) à partir du lien "img"
     * On renvoie les bytes.
     */
    private byte[] downloadImage(String link) {
        try {
            Request req = new Request.Builder()
                    .url(link)
                    .get()
                    .build();
            Response resp = stableHordeClient.newCall(req).execute();
            if (!resp.isSuccessful() || resp.body() == null) {
                return null;
            }
            return resp.body().bytes();
        } catch (IOException e) {
            return null;
        }
    }
}
