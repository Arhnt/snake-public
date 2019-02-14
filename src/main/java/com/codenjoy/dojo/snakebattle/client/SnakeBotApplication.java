package com.codenjoy.dojo.snakebattle.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 - 2019 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.client.WebSocketRunner;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Log4j2
public class SnakeBotApplication {

        private static final String EMAIL = "1tjpx2udkt9ci1tetg1l";
    private static final String TOKEN = "8828572629780502244";
//    private static final String EMAIL = "a@u.net";
//    private static final String TOKEN = "2130308101308439338";
    private static final String SERVER = "game3.epam-bot-challenge.com.ua";
        private static final String URL = "https://" + SERVER + "/codenjoy-contest/board/player/" + EMAIL + "?code=" + TOKEN;
//    private static final String URL = "https://snakebattle.tk/codenjoy-contest/board/player/" + EMAIL + "?code=" + TOKEN;
//    private static final String RESET_URL = "https://snakebattle.tk/codenjoy-contest/rest/player/" + EMAIL + "/" + TOKEN + "/reset";
    private static final String RESET_URL = "https://" + SERVER + "/codenjoy-contest/rest/player/" + EMAIL + "/" + TOKEN + "/reset";
    private static long lastReset;

    private static WebSocketRunner client;
    private static YourSolver solver;


    public static void main(String[] args) throws Exception {
        log.info("Application started");
        WebSocketRunner.ATTEMPTS = 100;
        WebSocketRunner.PRINT_TO_CONSOLE = true;
        startClient();

        while (true) {
            if (System.currentTimeMillis() - solver.getLastServerResponse() > 1500) {
                log.warn("Got delay form server - last response: {} ms ago", System.currentTimeMillis() - solver.getLastServerResponse());
                startClient();
            } else if (solver.isShouldReset()) {
                log.warn("Received reset request.");
                if (System.currentTimeMillis() - lastReset < 10000) {
                    log.warn("Reset was performed {} ms ago, please wait.", System.currentTimeMillis() - lastReset);
                } else {
                    log.warn("Resetting room");
                    lastReset = System.currentTimeMillis();
//                    resetRoom();
                }
            }
            Thread.sleep(200);
        }
    }

    private static void startClient() {
        log.info("Starting new client.");
        if (client != null) {
            client.close();
        }
        solver = new YourSolver();
        client = WebSocketRunner.runClient(URL, solver, new Board());
    }

    private static boolean resetRoom() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(new HttpGet(RESET_URL))) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream rspStream = entity.getContent();
                    try {
                        String resetResponse = IOUtils.toString(rspStream, Charset.defaultCharset());
                        log.debug("Response for reset request {}", resetResponse);
                        return "true".equals(resetResponse);
                    } finally {
                        rspStream.close();
                    }
                } else {
                    log.error("Got empty response for reset request.");
                    return false;
                }
            }
        } catch (IOException e) {
            log.error("Cannot reset room", e);
            return false;
        }
    }

}
