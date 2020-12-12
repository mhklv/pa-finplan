# Введение

Информационная система для организации и автоматизации финансового
планирования ООПТ. Имеет клиент-серверную архитектуру. Клиент и сервер
написаны на Java, клиент с использованием JavaFX. На данный момент
используется в нескольких ООПТ.


# Сборка

Проект разелён на 3 модуля:

* `finplan-server` --- серверный модуль;
* `finplan-client` --- клиентский модуль;
* `finplan-common` --- классы, используемые как на клиенте, так и на сервере.

Для сборки используется Maven 3.x и JDK 11 или выше; каждый модуль собирается отдельно:
```
[finplan-common]$ mvn clean install
[finplan-client]$ mvn clean package
[finplan-server]$ mvn clean package
```
После этого соответствующие jar-файлы расположены в директориях `finplan-server/shade` и `finplan-client/shade`.

При необходимости можно собрать native-executable с помощью `jpackage`:
```
[~]$ jpackage --input finplan-input/ --name finplan --main-jar finplan-client.jar --type msi --win-shortcut --win-dir-chooser --win-menu --win-upgrade-uuid 8e588e52-15dc-4d95-a813-df8f82a794dc --icon app-images/app_icon_256x256.ico --app-version 0.x
```

# Настройка сервера

Для развёртывания серверного модуля понадобится СУБД MariaDB 10 или MySQL 8 или совместимые с ними на уровне SQL.

Конфигурация серверного модуля `finplan-server.jar` происходит через аргументы командной строки:
* `-dbaddr` --- IP адрес (или доменное имя) СУБД;
* `-dbname` --- название базы данных, которую будет использовать серверный модуль;
* `-dbpass` --- пароль пользователя СУБД;
* `-dbusername` --- имя пользователя СУБД;
* `-port` --- номер порта, на котором серверный модуль будет слушать входящие соединения от клиентских модулей.

Пример запуска серверного модуля:
```
[~]$ java -jar finplan-server-jar-with-dependencies.jar -dbaddr localhost -dbname finplandb -dbpass 123 -dbusername finplan_user -port 33335
```
