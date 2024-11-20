package com.ll;

import com.llwiseSaying.WiseSaying;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }
}

class App {
    // 상대 경로로 설정
    private static final String DIRECTORY = "src/main/java/com/ll/db/wiseSaying/";
    private static final String DATA_FILE = "src/main/java/com/ll/db/wiseSaying/data.json";
    private int lastId;
    private WiseSaying[] wiseSayings;

    public App() {
        wiseSayings = new WiseSaying[100];
        loadLastId();
        loadWiseSayings();
    }

    public void run() {
        System.out.println("== 명언 앱 ==");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("명령) ");
            String cmd = scanner.nextLine();

            if (cmd.equals("종료")) {
                break;
            } else if (cmd.equals("등록")) {
                System.out.print("명언 : ");
                String content = scanner.nextLine();
                System.out.print("작가 : ");
                String author = scanner.nextLine();
                int id = ++lastId;

                WiseSaying wiseSaying = new WiseSaying(id, content, author);
                wiseSayings[id] = wiseSaying;

                saveWiseSaying(wiseSaying);
                saveLastId(lastId);

                System.out.println("%d번 명언이 등록되었습니다.".formatted(id));

            } else if (cmd.equals("목록")) {
                System.out.println("번호 / 작가 / 명언");
                System.out.println("----------------------");
                for (int i = 1; i <= lastId; i++) {
                    WiseSaying wiseSaying = wiseSayings[i];
                    if (wiseSaying != null) {
                        System.out.println(wiseSaying.id + " / " + wiseSaying.author + " / " + wiseSaying.content);
                    }
                }
            } else if (cmd.startsWith("삭제?id=")) {
                String idStr = cmd.substring(6);
                int id = Integer.parseInt(idStr);

                if (id > 0 && id <= lastId && wiseSayings[id] != null) {
                    wiseSayings[id] = null;
                    deleteWiseSayingFile(id);
                    System.out.println(id + "번 명언이 삭제되었습니다.");
                } else {
                    System.out.println(id + "번 명언은 존재하지 않습니다.");
                }
            } else if (cmd.startsWith("수정?id=")) {
                String idStr = cmd.substring(6);
                int id = Integer.parseInt(idStr);

                if (id > 0 && id <= lastId && wiseSayings[id] != null) {
                    System.out.println("명언(기존) : " + wiseSayings[id].content);
                    System.out.print("명령) ");
                    String newContent = scanner.nextLine();

                    System.out.println("작가(기존) : " + wiseSayings[id].author);
                    System.out.print("명령) ");
                    String newAuthor = scanner.nextLine();

                    wiseSayings[id].content = newContent;
                    wiseSayings[id].author = newAuthor;

                    saveWiseSaying(wiseSayings[id]); // 수정 후 다시 저장
                } else {
                    System.out.println(id + "번 명언은 존재하지 않습니다.");
                }
            } else if (cmd.equals("빌드")) {
                buildDataFile();
            }
        }

        scanner.close();
    }

    private void loadLastId() {
        try (BufferedReader reader = new BufferedReader(new FileReader(DIRECTORY + "lastId.txt"))) {
            lastId = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            lastId = 0; // 파일이 없거나 읽을 수 없으면 0으로 초기화
        }
    }

    private void loadWiseSayings() {
        for (int i = 1; i <= lastId; i++) {
            File file = new File(DIRECTORY + i + ".json");
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line = reader.readLine();
                    if (line != null) {
                        // JSON 형식으로 파싱
                        String[] parts = line.replace("{", "").replace("}", "").replace("\"", "").split(",");
                        int id = 0;
                        String content = "";
                        String author = "";

                        for (String part : parts) {
                            String[] keyValue = part.split(":");
                            if (keyValue[0].trim().equals("id")) {
                                id = Integer.parseInt(keyValue[1].trim());
                            } else if (keyValue[0].trim().equals("content")) {
                                content = keyValue[1].trim();
                            } else if (keyValue[0].trim().equals("author")) {
                                author = keyValue[1].trim();
                            }
                        }

                        wiseSayings[i] = new WiseSaying(id, content, author);
                    }
                } catch (IOException e) {
                    System.out.println("파일 읽기 오류: " + e.getMessage());
                }
            }
        }
    }

    private void saveWiseSaying(WiseSaying wiseSaying) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DIRECTORY + wiseSaying.id + ".json"))) {
            String json = String.format("{\"id\": %d, \"content\": \"%s\", \"author\": \"%s\"}",
                    wiseSaying.id, wiseSaying.content, wiseSaying.author);
            writer.write(json);
        } catch (IOException e) {
            System.out.println("파일 쓰기 오류: " + e.getMessage());
        }
    }

    private void saveLastId(int lastId) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DIRECTORY + "lastId.txt"))) {
            writer.write(String.valueOf(lastId));
        } catch (IOException e) {
            System.out.println("lastId 파일 쓰기 오류: " + e.getMessage());
        }
    }

    private void deleteWiseSayingFile(int id) {
        File file = new File(DIRECTORY + id + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    // 새로운 메서드: data.json 파일 생성
    private void buildDataFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            writer.write("[\n");
            for (int i = 1; i <= lastId; i++) {
                WiseSaying wiseSaying = wiseSayings[i];
                if (wiseSaying != null) {
                    String json = String.format("  {\n    \"id\": %d,\n    \"content\": \"%s\",\n    \"author\": \"%s\"\n  }",
                            wiseSaying.id, wiseSaying.content, wiseSaying.author);
                    writer.write(json);
                    if (i < lastId) {
                        writer.write(",\n"); // 마지막 항목이 아닐 경우 쉼표 추가
                    } else {
                        writer.write("\n"); // 마지막 항목인 경우 줄바꿈
                    }
                }
            }
            writer.write("]");
            System.out.println("data.json 파일의 내용이 갱신되었습니다.");
        } catch (IOException e) {
            System.out.println("data.json 파일 쓰기 오류: " + e.getMessage());
        }
    }
}


