package com.project.TelegramBot.TelegramBotDemo.bot;

import com.project.TelegramBot.TelegramBotDemo.config.BotConfig;
import com.project.TelegramBot.TelegramBotDemo.exception.ServiceException;
import com.project.TelegramBot.TelegramBotDemo.service.ExchangeRatesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig botConfig;

    private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class);

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String CAD = "/cad";
    private static final String GBP = "/gbp";
    private static final String CHF = "/chf";
    private static final String CNY = "/cny";
    private static final String CONVERT = "/converter";
    private static final String HELP = "/help";

    Boolean isConverter = false;

    @Autowired
    private ExchangeRatesService exchangeRatesService;

    public TelegramBot(@Value("${bot.token}") String botToken, BotConfig botConfig) {
        super(botToken);
        this.botConfig = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        if (isConverter) {
            convertEndCommand(chatId, update);
        } else if (update.hasCallbackQuery()) {
            String callBackQuery = update.getCallbackQuery().getData();
            Long chatIdCallBack = update.getCallbackQuery().getMessage().getChatId();

            if (callBackQuery.equals("CONVERTER_BUTTON")) {
                convertCommand(chatIdCallBack);
            } else if (callBackQuery.equals("HELP_CONVERTER_COMMAND")) {
                var text = """
                            Для конвертации валюты введите запрос следующего формата:
                            'usd 10.0', где
                            usd - код валюты,
                            10.0 - значение валюты, которое необходимо конвертировать.

                            Коды валют, с которыми работает бот:
                            usd - доллар США;
                            eur - евро;
                            cad - канадский доллар;
                            gbp - фунт стерлингов;
                            chf - швейцарский франк;
                            cny - китайский юань.
                            """;
                sendMessageWithButtonConverter(chatIdCallBack, text);
            } else if (callBackQuery.equals("HELP")) {
                helpCommand(chatIdCallBack);
            }
        } else {
            switch (message) {
                case START -> {
                    String userName = update.getMessage().getChat().getUserName();
                    startCommand(chatId, userName);
                }
                case USD -> {
                    usdCommand(chatId);
                }
                case EUR -> {
                    eurCommand(chatId);
                }
                case CAD -> {
                    cadCommand(chatId);
                }
                case GBP -> {
                    gbpCommand(chatId);
                }
                case CHF -> {
                    chfCommand(chatId);
                }
                case CNY -> {
                    cnyCommand(chatId);
                }
                case CONVERT -> {
                    convertCommand(chatId);
                }
                case HELP -> {
                    helpCommand(chatId);
                }
                default -> unknownCommand(chatId);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    private void startCommand (Long chatId, String userName) {
        var text = """
                Добро пожаловать, %s!

                Здесь Вы сможете узнать официальные курсы валют на сегодня, установленные ЦБ РФ.

                Для этого воспользуйтесь командами:
                /usd - курс доллара США;
                /eur - курс евро;
                /cad - курс канадского доллара;
                /gbp - курс фунта стерлингов;
                /chf - курс швейцарского франка;
                /cny - курс китайского юаня:
                /converter - режим конвертации валюты;
                /help - справочная информация.
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;

        try {
            var usd = exchangeRatesService.getUSDExchangeRate().replace(',', '.');
            var text = "Курс доллара США на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(usd)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса доллара США", e);
            formattedText = "Не удалось получить текущий курс доллара США. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;

        try {
            var eur = exchangeRatesService.getEURExchangeRate().replace(',', '.');
            var text = "Курс евро на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(eur)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса евро", e);
            formattedText = "Не удалось получить текущий курс евро. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void cadCommand(Long chatId) {
        String formattedText;

        try {
            var cad = exchangeRatesService.getCADExchangeRate().replace(',', '.');
            var text = "Курс канадского доллара на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(cad)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса канадского доллара", e);
            formattedText = "Не удалось получить текущий курс канадского доллара. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void gbpCommand(Long chatId) {
        String formattedText;

        try {
            var gbp = exchangeRatesService.getGBPExchangeRate().replace(',', '.');
            var text = "Курс фунта стерлингов на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(gbp)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса фунта стерлингов", e);
            formattedText = "Не удалось получить текущий курс фунта стерлингов. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void chfCommand(Long chatId) {
        String formattedText;

        try {
            var chf = exchangeRatesService.getCHFExchangeRate().replace(',', '.');
            var text = "Курс швейцарского франка на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(chf)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса швейцарского франка", e);
            formattedText = "Не удалось получить текущий курс швейцарского франка. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void cnyCommand(Long chatId) {
        String formattedText;

        try {
            var cny = exchangeRatesService.getCNYExchangeRate().replace(',', '.');
            var text = "Курс китайского юаня на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), String.format("%.2f", Double.parseDouble(cny)));
        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса китайского юаня", e);
            formattedText = "Не удалось получить текущий курс китайского юаня. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void convertCommand(Long chatId) {

        String formattedText = """
                Вы запустили режим "Конвертации валюты".
                Вы можете конвертировать выбранную валюту в рубли по текущему курсу ЦБ РФ.
                
                Для конвертации валюты введите запрос следующего формата:
                'usd 10.0', где
                usd - код валюты,
                10.0 - значение валюты, которое необходимо конвертировать.
                
                Коды валют, с которыми работает бот:
                usd - доллар США;
                eur - евро;
                cad - канадский доллар;
                gbp - фунт стерлингов;
                chf - швейцарский франк;
                cny - китайский юань.
                
                Для того, чтобы выйти из режима "Конвертации валюты" введите команду 'end'.
                """;
        isConverter = true;
        sendMessage(chatId, formattedText);
    }

    private void convertEndCommand(Long chatId, Update update) {

        String request = update.getMessage().getText();
        String currency;
        Double value = 0d;
        if (request.equals("end")) {
            currency = request;
        } else {
            currency = request.substring(0, 3);
            try {
                value = Double.parseDouble(request.substring(4));
            } catch (Exception e) {
                currency = "abc";
            }
        }
        Double currencyRate = 0d;
        String formattedText = "";

        switch (currency) {
            case "usd" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getUSDExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    if (value >= 5 && value <= 20) {
                        var text = "%s долларов США = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 1 || value%10 == 1) {
                        var text = "%s доллар США = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 2 || value%10 == 2 || value%10 == 3 || value%10 == 4) {
                        var text = "%s доллара США = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else {
                        var text = "%s долларов США = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    }
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса доллара США.", e);
                    formattedText = "Не удалось получить текущий курс доллара США. Попробуйте позже.";
                }
            }

            case "eur" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getEURExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    var text = "%s евро = %s руб. (По курсу ЦБ РФ на %s)";
                    formattedText = String.format(text, String.format("%.2f", value), LocalDate.now(), String.format("%.2f", result));
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса евро.", e);
                    formattedText = "Не удалось получить текущий курс евро. Попробуйте позже.";
                }
            }

            case "cad" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getCADExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    if (value >= 5 && value <= 20) {
                        var text = "%s канадских долларов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 1 || value%10 == 1) {
                        var text = "%s канадский доллар = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 2 || value%10 == 2 || value%10 == 3 || value%10 == 4) {
                        var text = "%s канадских доллара = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else {
                        var text = "%s канадских долларов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    }
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса канадского доллара.", e);
                    formattedText = "Не удалось получить текущий курс канадского доллара. Попробуйте позже.";
                }
            }

            case "gbp" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getGBPExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    if (value >= 5 && value <= 20) {
                        var text = "%s фунтов стерлингов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 1 || value%10 == 1) {
                        var text = "%s фунт сткрлингов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 2 || value%10 == 2 || value%10 == 3 || value%10 == 4) {
                        var text = "%s фунта стерлингов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else {
                        var text = "%s фунтов стерлингов = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    }
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса фунта стерлингов.", e);
                    formattedText = "Не удалось получить текущий курс фунта стерлингов. Попробуйте позже.";
                }
            }

            case "chf" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getCHFExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    if (value >= 5 && value <= 20) {
                        var text = "%s швейцарских франков = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 1 || value%10 == 1) {
                        var text = "%s швейцарский франк = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 2 || value%10 == 2 || value%10 == 3 || value%10 == 4) {
                        var text = "%s швейцарских франка = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else {
                        var text = "%s швейцарских франков = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    }
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса швейцарского франка.", e);
                    formattedText = "Не удалось получить текущий курс швейцарского франка. Попробуйте позже.";
                }
            }

            case "cny" -> {
                try {
                    currencyRate = Double.parseDouble(exchangeRatesService.getCNYExchangeRate().replace(',', '.'));
                    Double result = value * currencyRate;
                    if (value >= 5 && value <= 20) {
                        var text = "%s китайских юаней = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 1 || value%10 == 1) {
                        var text = "%s китайский юань = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else if (value == 2 || value%10 == 2 || value%10 == 3 || value%10 == 4) {
                        var text = "%s китайских юаня = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    } else {
                        var text = "%s китайских юаней = %s руб. (По курсу ЦБ РФ на %s)";
                        formattedText = String.format(text, String.format("%.2f", value), String.format("%.2f", result), LocalDate.now());
                    }
                } catch (ServiceException e) {
                    LOG.error("Ошибка получения курса китайского юаня.", e);
                    formattedText = "Не удалось получить текущий курс китайского юаня. Попробуйте позже.";
                }
            }

            case "end" -> {
                formattedText = "Вы вышли из режима \"Конвертации валюты\".";
            }

            default -> {
                var text = """
                Вы ввели неверный запрос.

                Запустите заново режим конвертации валюты, а затем введите корректный запрос на конвертацию.
                Либо воспользуйтесь справочной информацией.
                """;
                sendMessageWithButtons(chatId, text);
            }
        }

        if (!formattedText.isEmpty()) {
            sendMessage(chatId, formattedText);
        }

        isConverter = false;
    }

    private void helpCommand (Long chatId) {
        var text = """
                Справочная информация.
                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара;
                /eur - курс евро;
                /cad - курс канадского доллара;
                /gbp - курс фунта стерлингов;
                /chf - курс швейцарского франка;
                /cny - курс китайского юаня;
                /converter - режим конвертации валюты.
                """;
        sendMessage(chatId, text);
    }

    private void unknownCommand (Long chatId) {
        var text = """
                Не удалось распознать команду!
                
                Для получения текущих курсов валют воспользуйтесь командами:
                /usd - курс доллара;
                /eur - курс евро;
                /cad - курс канадского доллара;
                /gbp - курс фунта стерлингов;
                /chf - курс швейцарского франка;
                /cny - курс китайского юаня;
                /converter - режим конвертации валюты.
                """;
        sendMessage(chatId, text);
    }

    private void sendMessage (Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("/usd");
        row.add("/eur");
        row.add("/cad");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("/gbp");
        row.add("/chf");
        row.add("/cny");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("/converter");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void sendMessageWithButtonConverter(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var converterButton = new InlineKeyboardButton();
        converterButton.setText("Converter");
        converterButton.setCallbackData("CONVERTER_BUTTON ");

        rowInLine.add(converterButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);

        sendMessage.setReplyMarkup(markupInLine);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void sendMessageWithButtons(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();

        var converterButton = new InlineKeyboardButton();
        converterButton.setText("Режим \"Конвертации валюты\"");
        converterButton.setCallbackData("CONVERTER_BUTTON");

        var helpConverterButton = new InlineKeyboardButton();
        helpConverterButton.setText("Help \"Конвертация валюты\"");
        helpConverterButton.setCallbackData("HELP_CONVERTER_COMMAND");

        var helpButton = new InlineKeyboardButton();
        helpButton.setText("Help");
        helpButton.setCallbackData("HELP");

        rowInLine1.add(converterButton);
        rowsInLine.add(rowInLine1);

        rowInLine2.add(helpConverterButton);
        rowsInLine.add(rowInLine2);

        rowInLine3.add(helpButton);
        rowsInLine.add(rowInLine3);

        markupInLine.setKeyboard(rowsInLine);

        sendMessage.setReplyMarkup(markupInLine);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }
}