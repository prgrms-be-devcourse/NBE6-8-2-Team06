package com.back.domain.book.book.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 알라딘 API 수동 테스트 클래스
 * 실제 API 호출을 통해 응답을 확인할 수 있습니다.
 */
class AladinApiManualTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_KEY = "ttbsnake10101245001"; // 실제 키로 변경 필요
    private final String BASE_URL = "http://www.aladin.co.kr/ttb/api";

    @Test
    @DisplayName("알라딘 API 책 검색 테스트")
    void testAladinBookSearch() {

        try {
            // 검색 URL 구성
            String url = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=Book&output=js&Version=20131101",
                    BASE_URL, API_KEY, "자바", 5, 1
            );

            System.out.println("요청 URL: " + url);

            // API 호출
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("API 응답:");
            System.out.println(response);

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray()) {
                System.out.println("\n검색된 책 목록:");
                for (int i = 0; i < itemsNode.size(); i++) {
                    JsonNode item = itemsNode.get(i);
                    String title = getJsonValue(item, "title");
                    String publisher = getJsonValue(item, "publisher");
                    String isbn13 = getJsonValue(item, "isbn13");
                    String cover = getJsonValue(item, "cover");

                    System.out.printf("%d. 제목: %s%n", i + 1, title);
                    System.out.printf("   출판사: %s%n", publisher);
                    System.out.printf("   ISBN13: %s%n", isbn13);
                    System.out.printf("   표지: %s%n", cover);
                    System.out.println();
                }
            } else {
                System.out.println("검색 결과가 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("알라딘 API ISBN으로 책 조회 테스트")
    void testAladinBookLookup() {

        try {
            String isbn = "9788966261024"; // 테스트용 ISBN
            String url = String.format(
                    "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=packing",
                    BASE_URL, API_KEY, isbn
            );

            System.out.println("요청 URL: " + url);

            // API 호출
            String response = restTemplate.getForObject(url, String.class);

            System.out.println("API 응답:");
            System.out.println(response);

            // JSON 파싱
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode item = itemsNode.get(0);

                System.out.println("\n조회된 책 정보:");
                System.out.println("제목: " + getJsonValue(item, "title"));
                System.out.println("출판사: " + getJsonValue(item, "publisher"));
                System.out.println("ISBN13: " + getJsonValue(item, "isbn13"));
                System.out.println("총 페이지: " + getJsonValue(item, "itemPage"));
                System.out.println("출간일: " + getJsonValue(item, "pubDate"));
                System.out.println("표지: " + getJsonValue(item, "cover"));

                // 부가 정보 확인
                JsonNode subInfoNode = item.get("subInfo");
                if (subInfoNode != null) {
                    System.out.println("부제: " + getJsonValue(subInfoNode, "subTitle"));
                    System.out.println("쪽수: " + getJsonValue(subInfoNode, "itemPage"));
                }

            } else {
                System.out.println("해당 ISBN의 책을 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("알라딘 API 연결 테스트")
    void testAladinConnection() {
        try {
            // 가장 기본적인 검색 테스트
            String url = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=test&QueryType=Title&MaxResults=1&SearchTarget=Book&output=js&Version=20131101",
                    BASE_URL, API_KEY
            );

            String response = restTemplate.getForObject(url, String.class);

            if (response != null && response.contains("item")) {
                System.out.println("✅ 알라딘 API 연결 성공!");

                // 응답 구조 확인
                JsonNode rootNode = objectMapper.readTree(response);
                System.out.println("API 버전: " + rootNode.get("version"));
                System.out.println("총 결과 수: " + rootNode.get("totalResults"));

            } else {
                System.out.println("❌ 알라딘 API 응답이 예상과 다릅니다.");
                System.out.println("응답: " + response);
            }

        } catch (Exception e) {
            System.err.println("❌ 알라딘 API 연결 실패: " + e.getMessage());

            // 일반적인 오류 원인 안내
            System.err.println("\n가능한 원인:");
            System.err.println("1. API 키가 올바르지 않음");
            System.err.println("2. 네트워크 연결 문제");
            System.err.println("3. 알라딘 API 서버 문제");
            System.err.println("4. 요청 형식 오류");
        }
    }

    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }
}