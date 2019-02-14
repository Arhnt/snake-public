package com.codenjoy.dojo.snakebattle.strategy;

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

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RemoteStrategy {
//
//    private ObjectMapper mapper = new ObjectMapper();
//    private ExecutorService executorService = Executors.newFixedThreadPool(64);
//
//    public void nextAction(Board board) {
//        Map<Point, Integer> pois = BoardUtils.calcDistanceMatrix(board);
//        DistantStrategy.State state = new DistantStrategy.State(board.getMySnakeLength(), board.getMyFurry(), 0);
//
//        log.info("Submitting requests");
//        Map<Point, Future<Response>> futureRewards = pois.entrySet()
//            .stream()
//            .map(Map.Entry::getKey)
//            .parallel()
//            .collect(toMap(Function.identity(), point -> calculateReward(board, point, pois.get(point), state)));
//
//        log.info("Requests submitted");
//        Map<Point, Double> rewards = futureRewards.entrySet()
//            .stream()
//            .collect(Collectors.toMap(Map.Entry::getKey, e -> {
//                try {
//                    Response response = e.getValue().get();
//                    log.debug("Get response from server {}", response.getResponseBody());
//                    return Double.parseDouble(response.getResponseBody());
//                } catch (Exception ex) {
//                    return 0d;
//                }
//            }));
//
//        log.info("Rewards: {}", rewards);
//    }
//
//
//    private Future<Response> calculateReward(Board board, Point point, int movesToPoint, DistantStrategy.State state) {
//        RewardRequest request = new RewardRequest(board.boardAsString(), point, movesToPoint, state);
////        functions.operations().get().exe
//        return callRemoteFunction(request);
//    }
//
//    @SneakyThrows
//    private Future<Response> callRemoteFunction(RewardRequest request) {
//        String url = "https://5disy9puxe.execute-api.eu-central-1.amazonaws.com/default/distant-strategy-go";
//        String json = mapper.writeValueAsString(request);
//
//        AsyncHttpClient client = Dsl.asyncHttpClient();
//
//        BoundRequestBuilder post = client.preparePost(url)
//            .addHeader("Accept", "application/json")
//            .addHeader("Content-type", "application/json")
//            .addHeader("x-api-key", "tYJz8ELWPx1Vwm1MWJkGI4JPhlk8dP1SaPGCepOl")
//            .setCharset(Charset.forName("UTF-8"))
//            .setBody(json);
//        return post.execute();
//    }
//
//    @SneakyThrows
//    private Double callRemoteFunction1(RewardRequest request) {
//        String url = "https://europe-west1-epam-challenge.cloudfunctions.net/distant-strategy";
//        HttpPost post = new HttpPost(url);
//        post.setHeader("Accept", "application/json");
//        post.setHeader("Content-type", "application/json");
//        String json = mapper.writeValueAsString(request);
//        log.debug("Calling remote function with {}", json);
//        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
//
//        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//            try (CloseableHttpResponse response = httpClient.execute(post)) {
//                HttpEntity entity = response.getEntity();
//                if (entity != null) {
//                    InputStream rspStream = entity.getContent();
//                    try {
//                        String resetResponse = IOUtils.toString(rspStream, Charset.defaultCharset());
//                        log.debug("Response from remote function {}", resetResponse);
//                        return Double.parseDouble(resetResponse);
//                    } finally {
//                        rspStream.close();
//                    }
//                } else {
//                    log.error("Got empty response for remote function.");
//                    return 0d;
//                }
//            }
//        } catch (IOException e) {
//            log.error("Cannot call remote function", e);
//            return 0d;
//        }
//    }
//
//    @Data
//    @AllArgsConstructor
//    public static class RewardRequest {
//        private String boardString;
//        private Point origin;
//        private int movesToPoint;
//        private DistantStrategy.State state;
//    }
//
}
