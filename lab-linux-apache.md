# Лабораторная работа. Установка и настройка веб-сервера

Дисциплина: администрирование информационных систем.

## Цель

Ознакомиться с веб-серверами, которые ставят в Linux, и научиться установить Apache, поменять корень сайта и проверить ответ по адресу `http://localhost`.

## Оборудование

Компьютер с Debian или Ubuntu и права `sudo`. На Fedora и RHEL пакет называется `httpd`, служба тоже `httpd`, пути конфигурации другие. Ниже команды для Debian и Ubuntu.

## 1. Какой сервер ставят

Apache — обычный выбор для учебного и корпоративного сайта, конфигурация в файлах каталога `/etc/apache2`. Nginx чаще ставят туда, где много одновременных соединений и отдача статики. Lighttpd — небольшой сервер для простых задач. В этой работе устанавливается Apache, пакет `apache2`.

## 2. Установка

```bash
sudo apt update
sudo apt install apache2
systemctl status apache2
```

После установки служба включена и слушает порт 80. Проверка с самой машины:

```bash
curl -I http://localhost
```

Первая строка ответа — `HTTP/1.1 200 OK`. В браузере тот же адрес открывает страницу Apache по умолчанию из `/var/www/html`.

## 3. Где лежит конфигурация

В методичке указан файл `/etc/apache2/sites-available/default`. Так сайт назывался в Apache 2.2. В текущих Debian и Ubuntu файл сайта — `/etc/apache2/sites-available/000-default.conf`. В нём задан корень:

```apache
DocumentRoot /var/www/html
```

Каталог прав для этого пути описан отдельно, чаще в `/etc/apache2/apache2.conf`:

```apache
<Directory /var/www/>
        Options Indexes FollowSymLinks
        AllowOverride None
        Require all granted
</Directory>
```

`DocumentRoot` — каталог, из которого сервер отдаёт файлы. `Require all granted` разрешает HTTP-доступ к каталогу. Это не права Linux `chmod` и не «полный доступ всех пользователей к системе». `AllowOverride` решает, можно ли переопределять настройки файлом `.htaccess`: `None` запрещает, `All` разрешает. Для учебного сайта оставляют `None`.

## 4. Свой корень сайта

```bash
sudo mkdir -p /var/www/mysite
echo '<!DOCTYPE html><html><head><meta charset="utf-8"><title>mysite</title></head><body><h1>mysite</h1></body></html>' | sudo tee /var/www/mysite/index.html
sudo chown -R www-data:www-data /var/www/mysite
```

В `000-default.conf` меняют корень и добавляют блок каталога. Редактируют через `sudoedit` или `sudo nano`.

```apache
DocumentRoot /var/www/mysite

<Directory /var/www/mysite>
        Options Indexes FollowSymLinks
        AllowOverride None
        Require all granted
</Directory>
```

Старый `DocumentRoot /var/www/html` в этом файле не оставляют. Проверка синтаксиса и перезапуск:

```bash
sudo apache2ctl configtest
sudo systemctl restart apache2
curl -s http://localhost
```

`configtest` должен закончиться строкой `Syntax OK`. `curl` печатает страницу с заголовком `mysite`. В браузере `http://localhost` показывает то же.

Вернуть учебный сайт к исходному корню можно обратной заменой на `/var/www/html` и тем же `configtest` с перезапуском.

## 5. Что ещё проверяют

```bash
systemctl is-enabled apache2
ss -ltnp | grep ':80'
```

Служба должна быть `enabled`, порт 80 — в состоянии `LISTEN`. Журнал ошибок: `/var/log/apache2/error.log`.

## Вывод

На Debian и Ubuntu Apache ставят пакетом `apache2`. Корень сайта задаёт `DocumentRoot` в `/etc/apache2/sites-available/000-default.conf`, а доступ к каталогу — блок `Directory` с `Require all granted`. Перед перезапуском синтаксис проверяет `apache2ctl configtest`, затем `systemctl restart apache2`. Работающий сервер отвечает на `http://localhost` кодом 200 и отдаёт `index.html` из нового корня.
