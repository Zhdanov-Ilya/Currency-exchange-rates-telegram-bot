package com.project.TelegramBot.TelegramBotDemo.service;

import com.project.TelegramBot.TelegramBotDemo.exception.ServiceException;

public interface ExchangeRatesService {

    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;

    String getCADExchangeRate() throws ServiceException;

    String getGBPExchangeRate() throws ServiceException;

    String getCHFExchangeRate() throws ServiceException;

    String getCNYExchangeRate() throws ServiceException;
}
