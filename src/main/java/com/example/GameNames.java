package com.example;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class GameNames {

    public static void main(String[] args) throws Exception {

       ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");

WebDriver driver = new ChromeDriver(options);

driver.get("https://nakwin44.ibs.com");

System.out.println(driver.getCurrentUrl());
System.out.println(driver.getTitle());

driver.quit();
        }
    }
