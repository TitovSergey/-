# Мониторинг процессов, автозагрузка и cron

Дисциплина: администрирование информационных систем.

Скрипт: [monitor.sh](monitor.sh). Юнит автозагрузки: [monitor.service](monitor.service).

## 1. Скрипт monitor.sh

Скрипт каждые 5 секунд дописывает в `processes.log` дату и вывод `ps -ef`. Файл журнала лежит рядом со скриптом. Каталог можно сменить переменной `LOG_FILE`.

Запуск цикла:

```bash
chmod +x monitor.sh
./monitor.sh
```

Остановка: Ctrl+C в этом терминале или `kill` по PID скрипта, не по PID 1.

Один снимок без ожидания:

```bash
./monitor.sh --once
```

Повторный запуск цикла сразу завершается. Иначе автозагрузка и cron каждую минуту плодили бы новые бесконечные копии. За это отвечает `flock` и файл `monitor.lock`.

## 2. Автозагрузка

Скрипт копируют в системный каталог и включают юнит systemd. После этого цикл стартует при загрузке системы и пишет в `/var/log/processes.log`.

```bash
sudo cp monitor.sh /usr/local/sbin/monitor.sh
sudo chmod 755 /usr/local/sbin/monitor.sh
sudo cp monitor.service /etc/systemd/system/monitor.service
sudo systemctl daemon-reload
sudo systemctl enable --now monitor.service
systemctl status monitor.service
```

Проверка, что служба включена в автозагрузку:

```bash
systemctl is-enabled monitor.service
```

Ожидаемый ответ: `enabled`.

## 3. Задание cron

Cron запускает один снимок раз в минуту. Режим `--once` не открывает второй бесконечный цикл поверх службы.

```bash
crontab -e
```

Строка задания:

```cron
* * * * * /usr/local/sbin/monitor.sh --once
```

Пять звёзд означают: каждая минута, каждый час, каждый день месяца, каждый месяц, каждый день недели. Журнал тот же, `processes.log` рядом со скриптом, то есть `/usr/local/sbin/processes.log`. Чтобы писать в тот же файл, что и служба:

```cron
* * * * * LOG_FILE=/var/log/processes.log /usr/local/sbin/monitor.sh --once
```

Проверка, что задание записано:

```bash
crontab -l
```

## 4. Загрузка системы

Текущую загрузку показывает `uptime`: текущее время, время работы, число пользователей и средняя нагрузка за 1, 5 и 15 минут. Результат дописывают в `system_load.log`.

```bash
uptime | tee -a system_load.log
```

Повторный запуск добавляет новую строку, старые не стирает. Содержимое смотрят так:

```bash
cat system_load.log
```

На компьютере, где готовился этот отчёт, команды `uptime` нет: установлена Windows без Linux. Команду выполняют в терминале Linux.

## Замечание про два расписания

Служба уже пишет снимок каждые 5 секунд. Cron раз в минуту добавляет ещё одну запись тем же скриптом. Оба требования задания так выполнены, и число процессов monitor.sh не растёт.
