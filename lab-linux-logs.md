# Лабораторная работа. Логи

Дисциплина: администрирование информационных систем.

## Цель

Установить и включить rsyslog, записывать действия sudo, разложить сообщения уровней info, warning и err по разным файлам и настроить передачу журналов на второй свой сервер в каталоги вида `/var/log/rsyslog/%HOSTNAME%/%PROGRAMNAME%.log`.

## Оборудование

Две машины с Debian или Ubuntu, которыми вы сами распоряжаетесь: клиент, где возникают сообщения, и сервер, который их принимает. Подойдёт вторая виртуальная машина или клон своей системы. Чужой компьютер и машина соседа без договорённости сюда не входят. На обеих машинах нужны права `sudo`.

Ниже `SERVER_IP` заменяют на адрес своей второй машины.

## 1. Установка и запуск

На клиенте и на сервере:

```bash
sudo apt update
sudo apt install rsyslog
sudo systemctl enable rsyslog
sudo systemctl start rsyslog
systemctl status rsyslog
```

`enable` включает автозапуск, `start` запускает службу сразу. Проверка приёма локального сообщения:

```bash
logger -p user.notice "rsyslog lab"
sudo grep "rsyslog lab" /var/log/syslog
```

## 2. Журнал действий sudo

Файл `/etc/sudoers` меняют только через `visudo`: при ошибке синтаксиса обычный редактор может оставить систему без `sudo`.

```bash
sudo visudo
```

В конец файла:

```sudoers
Defaults syslog=authpriv
Defaults logfile=/var/log/sudo.log
```

Первая строка отправляет события sudo в syslog, канал `authpriv`. Вторая дополнительно пишет текстовый журнал `/var/log/sudo.log`.

Проверка:

```bash
sudo ls /root
sudo tail /var/log/sudo.log
```

В строке видны дата, имя пользователя и команда.

## 3. Разные уровни в разные файлы

Уровни syslog имеют номера: `err` — 3, `warning` — 4, `info` — 6. Обычная запись `*.info` захватывает и более серьёзные уровни, поэтому info, warning и err так не разделить. Нужно сравнение номера.

Файл на клиенте `/etc/rsyslog.d/10-levels.conf`:

```rsyslog
if $syslogseverity == 6 then {
    action(type="omfile" file="/var/log/rsyslog/info.log")
}
if $syslogseverity == 4 then {
    action(type="omfile" file="/var/log/rsyslog/warning.log")
}
if $syslogseverity == 3 then {
    action(type="omfile" file="/var/log/rsyslog/error.log")
}
```

```bash
sudo mkdir -p /var/log/rsyslog
sudo systemctl restart rsyslog
logger -p user.info "lab info"
logger -p user.warning "lab warning"
logger -p user.err "lab err"
sudo tail /var/log/rsyslog/info.log /var/log/rsyslog/warning.log /var/log/rsyslog/error.log
```

Каждая фраза оказывается только в своём файле.

## 4. Приём на сервере

На сервере открывают порт 514/tcp в своём межсетевом экране, если он включён, и только для адреса клиента.

Файл `/etc/rsyslog.d/20-receive.conf`:

```rsyslog
module(load="imtcp")
input(type="imtcp" port="514")

template(name="HostProg" type="string"
         string="/var/log/rsyslog/%HOSTNAME%/%PROGRAMNAME%.log")

action(type="omfile" dynaFile="HostProg")
```

`%HOSTNAME%` и `%PROGRAMNAME%` дают требуемый шаблон пути. Каталоги rsyslog создаёт сам.

```bash
sudo systemctl restart rsyslog
sudo ss -ltnp | grep 514
```

## 5. Отправка с клиента

Файл на клиенте `/etc/rsyslog.d/30-forward.conf`:

```rsyslog
action(type="omfwd" target="SERVER_IP" port="514" protocol="tcp"
       queue.type="LinkedList" queue.filename="fwd" action.resumeRetryCount="-1")
```

Очередь нужна, чтобы сообщения не пропадали, пока сервер недоступен. `SERVER_IP` — адрес своей второй машины.

```bash
sudo systemctl restart rsyslog
logger -t labprog "forward check"
```

На сервере появляется файл `/var/log/rsyslog/<имя-клиента>/labprog.log` со строкой `forward check`.

```bash
sudo ls /var/log/rsyslog
sudo tail /var/log/rsyslog/*/labprog.log
```

## Вывод

Пакет `rsyslog` ставят через `apt`, включают и запускают через `systemctl`. Действия sudo пишутся правилами `Defaults` в sudoers, файл правят через `visudo`. Уровни info, warning и err раскладываются по файлам сравнением `$syslogseverity`. Сервер принимает TCP 514 и складывает сообщения в `/var/log/rsyslog/%HOSTNAME%/%PROGRAMNAME%.log`. Клиент отправляет поток правилом `omfwd` на свой второй сервер.
