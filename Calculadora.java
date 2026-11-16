import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Calculadora {

    public static int somar(int a, int b) {
        return a + b;
    }

    public static int subtrair(int a, int b) {
        return a - b;
    }

    public static int multiplicar(int a, int b) {
        return a * b;
    }

    public static void main(String[] args) throws IOException {
        HttpServer servidor = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        servidor.createContext("/", Calculadora::atenderRequisicao);
        servidor.start();

        System.out.println("Calculadora disponível em http://localhost:8080");
    }

    private static void atenderRequisicao(HttpExchange requisicao) throws IOException {
        String caminho = requisicao.getRequestURI().getPath();
        String resposta;
        int status = 200;

        try {
            if ("/".equals(caminho)) {
                resposta = """
                    <!DOCTYPE html>
                    <html lang="pt-BR">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                        <title>Calculadora Java</title>
                    </head>
                    <body>
                        <h1>Calculadora Java</h1>
                        <p>Aplicação de teste para o pipeline CI/CD.</p>
                        <p>Use /somar?a=10&b=5</p>
                        <p>Use /subtrair?a=10&b=5</p>
                        <p>Use /multiplicar?a=10&b=5</p>
                        <p>Verificação: /health</p>
                    </body>
                    </html>
                    """;
                requisicao.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            } else if ("/health".equals(caminho)) {
                resposta = "{\"status\":\"ok\"}";
                requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            } else if ("/somar".equals(caminho)) {
                resposta = resultadoJson(somar(obterInteiro(requisicao, "a"), obterInteiro(requisicao, "b")));
                requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            } else if ("/subtrair".equals(caminho)) {
                resposta = resultadoJson(subtrair(obterInteiro(requisicao, "a"), obterInteiro(requisicao, "b")));
                requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            } else if ("/multiplicar".equals(caminho)) {
                resposta = resultadoJson(multiplicar(obterInteiro(requisicao, "a"), obterInteiro(requisicao, "b")));
                requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            } else {
                status = 404;
                resposta = "{\"erro\":\"Rota nao encontrada\"}";
                requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            }
        } catch (IllegalArgumentException erro) {
            status = 400;
            resposta = "{\"erro\":\"Informe os parametros inteiros a e b\"}";
            requisicao.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        }

        requisicao.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        requisicao.getResponseHeaders().set("X-Frame-Options", "DENY");
        requisicao.getResponseHeaders().set("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        requisicao.getResponseHeaders().set("Cache-Control", "no-store");

        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        requisicao.sendResponseHeaders(status, bytes.length);
        requisicao.getResponseBody().write(bytes);
        requisicao.close();
    }

    private static int obterInteiro(HttpExchange requisicao, String nome) {
        Map<String, String> parametros = new HashMap<>();
        String consulta = requisicao.getRequestURI().getRawQuery();

        if (consulta != null) {
            for (String parte : consulta.split("&")) {
                String[] chaveValor = parte.split("=", 2);
                if (chaveValor.length == 2) {
                    parametros.put(
                        URLDecoder.decode(chaveValor[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(chaveValor[1], StandardCharsets.UTF_8)
                    );
                }
            }
        }

        String valor = parametros.get(nome);
        if (valor == null) {
            throw new IllegalArgumentException("Parametro ausente: " + nome);
        }

        return Integer.parseInt(valor);
    }

    private static String resultadoJson(int resultado) {
        return "{\"resultado\":" + resultado + "}";
    }
}
