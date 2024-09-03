package br.com.alura.service;

import br.com.alura.client.ClientHttpConfiguration;
import br.com.alura.domain.Pet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PetService {

    private ClientHttpConfiguration client;

    public PetService(ClientHttpConfiguration client) {
        this.client = client;
    }

    public void listarPetsDoAbrigo() throws IOException, InterruptedException {
        System.out.println("Digite o id ou nome do abrigo:");
        String idOuNome = new Scanner(System.in).nextLine();

        String uri = "http://localhost:8080/abrigos/" +idOuNome +"/pets";

        HttpResponse<String> response = client.dispararRequisicaoGet(uri);

        int statusCode = response.statusCode();
        if (statusCode == 404 || statusCode == 500) {
            System.out.println("ID ou nome não cadastrado!");
        }
        String responseBody = response.body();
        Pet[] pets = new ObjectMapper().readValue(responseBody, Pet[].class);

        List<Pet> petList = Arrays.stream(pets).toList();
        System.out.println("Pets cadastrados:");
        for (Pet pet : petList) {
            long id = pet.getId();
            String tipo = pet.getTipo();
            String nome = pet.getNome();
            String raca = pet.getRaca();
            int idade = pet.getIdade();
            String cor = pet.getCor();
            Float peso = pet.getPeso();
            System.out.println(id +" - " +tipo +" - " +nome +" - " +raca +" - " +idade +" ano(s)");
        }
    }

    public void importarPetsDoAbrigo() throws IOException, InterruptedException {
        System.out.println("Digite o id ou nome do abrigo:");
        String idOuNome = new Scanner(System.in).nextLine();

        System.out.println("Digite o nome do arquivo CSV:");
        String nomeArquivo = new Scanner(System.in).nextLine();

        // Obtendo o diretório onde o Main está localizado
        String projectDir = "3149-boas-praticas-java-projeto_inicial";
        File file = new File(projectDir + File.separator + nomeArquivo);

        if (!file.exists()) {
            System.out.println("Arquivo não encontrado: " + file.getAbsolutePath());
            return;
        }else {
            System.out.println("Existe");
        }


        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            System.out.println("Erro ao carregar o arquivo: " + nomeArquivo);
            return; // Saia do método se o arquivo não puder ser carregado
        }

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] campos = line.split(",");
                String tipo = campos[0];
                String nome = campos[1];
                String raca = campos[2];
                int idade = Integer.parseInt(campos[3]);
                String cor = campos[4];
                Float peso = Float.parseFloat(campos[5]);

                Pet pet = new Pet(tipo, nome, raca, idade, cor, peso);

                // Imprimindo o objeto Pet para depuração
                System.out.println("Tentando cadastrar o seguinte pet:");
                System.out.println(pet);

                String uri = "http://localhost:8080/abrigos/" + idOuNome + "/pets";

                HttpResponse<String> response = client.dispararRequisicaoPost(uri, pet);
                int statusCode = response.statusCode();
                String responseBody = response.body();

                if (statusCode == 200) {
                    System.out.println("Pet cadastrado com sucesso: " + nome);
                } else if (statusCode == 404) {
                    System.out.println("Id ou nome do abrigo não encontrado!");
                    break;
                } else if (statusCode == 400 || statusCode == 500) {
                    System.out.println("Erro ao cadastrar o pet: " + nome);
                    System.out.println("Status Code: " + statusCode);
                    System.out.println("Response Body: " + responseBody);
                    break;
                }
            }

        } finally {
            if (reader != null) {
                reader.close(); // Certifique-se de fechar o reader no final
            }
        }
    }


    public HttpResponse<String> dispararRequisicaoGet(HttpClient client, String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> dispararRequisicaoPost(HttpClient client, String uri, JsonObject json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
