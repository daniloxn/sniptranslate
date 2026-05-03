package org.example;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OCRClient {

    private final OkHttpClient client;

    /**
     * Construtor que configura o OkHttpClient com timeouts para evitar travamentos.
     */
    public OCRClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
        // Comentário didático: Configuramos timeouts maiores para upload de imagens,
        // pois o processamento OCR pode demorar.
    }

    /**
     * Envia uma imagem local para a API OCR.space e retorna a resposta JSON bruta.
     *
     * @param filePath caminho completo para o arquivo de imagem local
     * @param apikey chave da API OCR.space
     * @param language código da linguagem (ex: 'eng' para inglês, 'por' para português)
     * @return resposta JSON bruta da API
     * @throws IOException em caso de erro na requisição ou arquivo não encontrado
     */
    public String parseImage(String filePath, String apikey, String language) throws IOException {
        // 1. Verificar e preparar o arquivo
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Arquivo não encontrado: " + filePath);
        }

        // 2. Montar MultipartBody com apikey, language e o arquivo
        // Comentário didático: Usamos MultipartBody para upload de arquivos via form-data.
        RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("apikey", apikey)
            .addFormDataPart("language", language)
            .addFormDataPart("file", file.getName(), fileBody)
            .build();

        // 3. Executar a requisição POST para a URL da API
        // Comentário didático: A URL é fixa conforme documentação do OCR.space.
        Request request = new Request.Builder()
            .url("https://api.ocr.space/parse/image")
            .post(requestBody)
            .build();

        // 4. Executar e capturar resposta
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro na API: " + response.code() + " - " + response.message());
            }
            // Comentário didático: Retornamos a resposta bruta em JSON sem parsing,
            // conforme solicitado.
            return response.body().string();
        }
    }

    // Exemplo de uso (para teste):
    /*
    public static void main(String[] args) {
        try {
            OCRClient ocr = new OCRClient();
            String result = ocr.parseImage("caminho/para/imagem.jpg", "SUA_APIKEY", "eng");
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
