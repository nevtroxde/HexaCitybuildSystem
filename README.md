# HexaCitybuildSystem (CBSystem)

Ein vielseitiges Minecraft-Plugin für Server Administration und Server Features.  

---

##  Kompatibilität

- **Minecraft**: Java Edition Version 1.21.x  
- **Plattformen**: Bukkit, Paper, Spigot  

---

##  Installation

1. Lade `CBSystem.jar` herunter und lege es in den Ordner `plugins/` deines Servers.  
2. Starte oder lade den Server neu.  
3. Konfiguriere das Plugin im Ordner `plugins/CBSystem/`.  
4. Vergib die notwendigen Berechtigungen an deine Spieler.  

---

##  Befehle & Berechtigungen

Die wichtigsten Befehle im Überblick:

| Befehl | Beschreibung | Berechtigung |
|--------|--------------|--------------|
| `/gamemode (0–3)` | Ändert deinen Game Mode | `cbsystem.gamemode` |
| `/gamemode (0–3) [player]` | Ändert Game Mode eines anderen | `cbsystem.gamemode.other` |
| `/fly [player]` | Flugmodus (selbst/anderer) | `cbsystem.fly`, `cbsystem.fly.other` |
| `/heal [player]` | Heilt dich / anderen | `cbsystem.heal`, `cbsystem.heal.other` |
| `/feed [player]` | Füttert dich / anderen | `cbsystem.feed`, `cbsystem.feed.other` |
| `/trash` | Öffnet Müllinterface | `cbsystem.trash` |
| `/head [player]` | Gibt Kopf aus | `cbsystem.head`, `cbsystem.head.other` |
| `/enderchest [player]` | Öffnet Ender-Chest (eigen/anderer) | `cbsystem.ec`, `cbsystem.ec.other` |
| `/invsee [player]` | Inventar eines anderen ansehen | `cbsystem.invsee` |
| `/speed [value] [player]` | Bewegungsgeschwindigkeit (Eigen/anderer) | `cbsystem.speed`, `cbsystem.speed.other` |
| `/give [item]` | Gibt einen Gegenstand | `cbsystem.give` |
| `/broadcast [message]` | Serverweite Nachricht senden | `cbsystem.broadcast` |
| `/slowchat` | Langsamen Chat aktivieren | `cbsystem.slowchat` |
| `/spawn`, `/setspawn` | Zum Spawn / Spawn setzen | `cbsystem.spawn`, `cbsystem.setspawn` |
| `/warp/setwarp/delwarp [warpname]` | Verwaltung von Warps | `cbsystem.warp`, `cbsystem.setwarp`, `cbsystem.delwarp` |
| `/tpo/tphere/tpa/tpahere` | Teleportation – verschiedene Modi | entsprechende Berechtigung je nach Befehl |
| `/tpaccept`, `/tpadeny`, `/tpatoggle` | Umgang mit Teleportanfragen | `cbsystem.tpaccept`, `cbsystem.tpadeny`, `cbsystem.tpatoggle` |
| `/playtime [player]` | Spielzeit (Eigen/anderer) | `cbsystem.playtime`, `cbsystem.playtime.other` |
| `/clear [player]` | Inventar leeren (Eigen/anderer) | `cbsystem.clear`, `cbsystem.clear.other` |
| `/vanish [player]` | Vanish aktivieren | `cbsystem.vanish`, `cbsystem.vanish.other` |
| `/maintenance` | Wartungsmodus aktivieren | `cbsystem.maintenance` |
| `/sign` | Item signieren | `cbsystem.sign` |
| `/sudo [player] [command]` | Befehl als andere:r ausführen | `cbsystem.sudo` |
| `/discord`, `/youtube` | Community-Links anzeigen | `cbsystem.discord`, `cbsystem.youtube` |
| `/teamchat` | Team Chat aktivieren | `cbsystem.teamchat` |
| `/message/ /respond` | Private Nachrichten | `cbsystem.message`, `cbsystem.respond` |
| `/spawnentity [entity]` | Entity spawnen | `cbsystem.spawnentity` |
| `/reload`, `/restart` | Plugin bzw. Server neu laden | `cbsystem.reload`, `cbsystem.restart` |
| `/homes`, `/home <name>` | Homes-Interface / Teleport zu Home | `cbsystem.home.use`, `cbsystem.home.teleport` |
| `/sethome <name>` | Home setzen | `cbsystem.home.set.*` |
| `/delhome <name>` | Home löschen | `cbsystem.home.delete` |
| `/bank` | Bank-GUI öffnen | `cbsystem.bank` |

> Für Vollzugriff: `cbsystem.*`  

---

##  Lizenz

Dieses Plugin ist unter der **CC-BY-NC-4.0** lizenziert. Du darfst es für **nicht-kommerzielle Zwecke** teilen und verändern – unter Nennung des Urhebers. Für **kommerziellen Einsatz** oder Weiterverteilung bitte den Autor kontaktieren.
Bei anfrage ist dies trotzdem möglich. Discord: NiceRecord

---

##  Support

- Prüfe die **Serverkonsole** auf Fehlermeldungen.  
- Stelle sicher, dass die **Berechtigungen korrekt gesetzt** sind.  
- Achte auf die **aktuellste Version des Plugins**.
- Für Support schreibe NiceRecord auf Discord an.

---
Made with ❤️ for Minecraft servers by **HexaPlugins**.  
