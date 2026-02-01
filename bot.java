import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SimpleBot {
    
    // ВАШ ТОКЕН от @BotFather
    private static final String TOKEN = "ВАШ_ТОКЕН";
    
    // ВАШ Telegram ID
    private static final String ADMIN_ID = "ВАШ_ID";
    
    private static final String API_URL = "https://api.telegram.org/bot" + TOKEN;
    private static long lastUpdateId = 0;
    
    public static void main(String[] args) {
        System.out.println("🎮 БОТ ДЛЯ ARKANOID ЗАПУЩЕН!");
        System.out.println("✅ Ожидаю команды /start ...");
        
        while (true) {
            try {
                checkMessages();
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("⚠️ Ошибка: " + e.getMessage());
            }
        }
    }
    
    private static void checkMessages() throws Exception {
        String url = API_URL + "/getUpdates?offset=" + (lastUpdateId + 1);
        String response = sendGetRequest(url);
        
        if (response == null) return;
        
        // Ищем новые сообщения
        if (response.contains("\"update_id\":")) {
            // Обновляем lastUpdateId
            String updateIdStr = response.split("\"update_id\":")[1].split(",")[0];
            lastUpdateId = Long.parseLong(updateIdStr);
            
            // Проверяем есть ли текст сообщения
            if (response.contains("\"text\":")) {
                String text = response.split("\"text\":\"")[1].split("\"")[0];
                String chatId = response.split("\"chat\":\\{\"id\":")[1].split(",")[0];
                String username = response.contains("\"username\":\"") ? 
                    response.split("\"username\":\"")[1].split("\"")[0] : "NoUsername";
                
                System.out.println("\n📨 Новое сообщение:");
                System.out.println("👤 От: " + username);
                System.out.println("💬 Текст: " + text);
                System.out.println("🆔 Chat ID: " + chatId);
                
                // Обрабатываем команду
                processMessage(chatId, text, username);
            }
        }
    }
    
    private static void processMessage(String chatId, String text, String username) {
        try {
            if (text.startsWith("/start withdraw__")) {
                // ЗАЯВКА НА ВЫВОД ИЗ ИГРЫ
                System.out.println("💰 ОБРАБАТЫВАЮ ЗАЯВКУ НА ВЫВОД!");
                
                // Разбираем команду: /start withdraw__METHOD__WALLET__COINS__ADS__NAME
                String[] parts = text.substring(7).split("__");
                
                if (parts.length >= 6) {
                    String method = parts[1];
                    String wallet = parts[2];
                    String coins = parts[3];
                    String ads = parts[4];
                    String name = parts[5];
                    
                    System.out.println("📋 Детали заявки:");
                    System.out.println("   Метод: " + method);
                    System.out.println("   Кошелек: " + wallet);
                    System.out.println("   Монеты: " + coins);
                    System.out.println("   Реклам: " + ads);
                    System.out.println("   Имя: " + name);
                    
                    // Отправляем ответ игроку
                    String replyToUser = "✅ ЗАЯВКА ПРИНЯТА!\n\n" +
                                       "👤 Игрок: " + name + "\n" +
                                       "💰 Сумма: " + coins + " монет\n" +
                                       "💳 Метод: " + method + "\n" +
                                       "🏦 Кошелек: " + wallet + "\n\n" +
                                       "⏳ Статус: В обработке\n" +
                                       "📞 Администратор свяжется с вами";
                    
                    sendMessage(chatId, replyToUser);
                    
                    // Отправляем уведомление админу (себе)
                    String toAdmin = "🚀 НОВАЯ ЗАЯВКА НА ВЫВОД!\n\n" +
                                   "👤 Игрок: " + name + "\n" +
                                   "📱 @" + username + "\n" +
                                   "🆔 ID: " + chatId + "\n\n" +
                                   "💰 " + coins + " монет\n" +
                                   "💳 " + method + ": " + wallet + "\n\n" +
                                   "✅ /approve_" + chatId + " - Одобрить\n" +
                                   "❌ /reject_" + chatId + " - Отклонить";
                    
                    sendMessage(ADMIN_ID, toAdmin);
                    
                    System.out.println("✅ Заявка обработана!");
                }
                
            } else if (text.equals("/start")) {
                // Простая команда /start
                String welcome = "🎮 Добро пожаловать в бот для Arkanoid!\n\n" +
                               "💰 Для вывода средств:\n" +
                               "1. Играйте в Arkanoid\n" +
                               "2. Заработайте 1000+ монет\n" +
                               "3. Нажмите 💰 в игре\n" +
                               "4. Заполните форму\n\n" +
                               "💱 Курс: 1000 монет = 1 рубль\n" +
                               "📞 Поддержка: @admin";
                
                sendMessage(chatId, welcome);
                
            } else if (text.equals("/help")) {
                String help = "🆘 ПОМОЩЬ\n\n" +
                            "💰 Как вывести деньги:\n" +
                            "1. Играйте и зарабатывайте\n" +
                            "2. Нажмите 💰 в игре\n" +
                            "3. Заполните форму вывода\n" +
                            "4. Подтвердите заявку\n\n" +
                            "📞 Вопросы: @admin";
                
                sendMessage(chatId, help);
                
            } else if (text.startsWith("/approve_")) {
                // Одобрение заявки (только админ)
                if (chatId.equals(ADMIN_ID)) {
                    String userId = text.substring(9);
                    sendMessage(userId, "🎉 Ваша заявка ОДОБРЕНА! Деньги поступят в течение 24 часов.");
                    sendMessage(ADMIN_ID, "✅ Заявка " + userId + " одобрена.");
                }
                
            } else if (text.startsWith("/reject_")) {
                // Отклонение заявки (только админ)
                if (chatId.equals(ADMIN_ID)) {
                    String userId = text.substring(8);
                    sendMessage(userId, "❌ Ваша заявка ОТКЛОНЕНА. Обратитесь к администратору.");
                    sendMessage(ADMIN_ID, "❌ Заявка " + userId + " отклонена.");
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Ошибка обработки: " + e.getMessage());
        }
    }
    
    private static void sendMessage(String chatId, String text) {
        try {
            String url = API_URL + "/sendMessage";
            String params = "chat_id=" + chatId + 
                          "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
            
            sendPostRequest(url, params);
            System.out.println("📤 Отправил сообщение в чат " + chatId);
        } catch (Exception e) {
            System.out.println("❌ Не могу отправить сообщение: " + e.getMessage());
        }
    }
    
    private static String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        
        if (conn.getResponseCode() == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        }
        return null;
    }
    
    private static void sendPostRequest(String urlString, String params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }
        
        conn.getResponseCode(); // Просто отправляем
    }
}
