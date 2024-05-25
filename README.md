# GlobalMultiEconomy

## Descripción

Este es un plugin para crear varias economías globales para que se compartan entre las diferentes modalidades de una
network.

## Características

- Te permite crear y eliminar todas las economías que quieras.
- Tienes una tabla en la BD creada con información sobre las transicciones de las diferentes economías.
- Puedes ver, añadir, y eliminar tu balance de cualquier economía.
- Puedes ver, añadir, y eliminar el balance de cualquier jugador en cualquier economía.

## Requisitos

### SoftDepends

- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (Opcional), Se usa para registrar
  placeholders del plugin y poder usarlas en cualquier otro plugin, el propio plugin cuenta con sus placeholders
  internas y no es necesario para el funcionamiento del mismo.

## Instalación

1. Descarga el plugin y colócalo en la carpeta `plugins` de tu servidor/es.
2. Inicia el servidor.
3. Configura el plugin a tu gusto, como el host, puerto, usuario, contraseña y nombre de la BD MariaDB.
4. Cambia los mensajes a tu gusto.
5. Reinicia el servidor para que el plugin pueda cargar tu BD.
6. ¡Listo! Ya puedes disfrutar del plugin.

## Uso

### Comandos de plugin

- Usa el comando `/gme help` para ver la lista de comandos disponibles.
- Usa el comando `/gme reload` para recargar la configuración del plugin.
- Usa el comando `/gme economy create <nombreEconomía>` para crear una nueva economía.
- Usa el comando `/gme economy delete <nombreEconomía>` para eliminar una economía existente, para confirmar el borrado
  de la economía escribe en el chat `DeLeTe <nombreEconomía>`.

### Comandos de economía

- Usa el comando `/<nombreEconomía>` para ver tu balance en una economía.
- Usa el comando `/<nombreEconomía> <jugador>` para ver el balance de un jugador en una economía.
- Usa el comando `/<nombreEconomía> give <jugador> <cantidad>` para añadir una cantidad de dinero a el balance de un
  jugador en una economía.
- Usa el comando `/<nombreEconomía> take <jugador> <cantidad>` para quitar una cantidad de dinero a el balance de un
  jugador en una economía.
- Usa el comando `/<nombreEconomía> set <jugador> <cantidad>` para establecer una cantidad de dinero a el balance de un
  jugador en una economía.

## Permisos

- `globalmultieconomy.admin` - Permite al usuario usar todos los comandos del plugin.
- `globalmultieconomy.economy.check.self` - Permite al usuario ver su balance en una economía.
- `globalmultieconomy.economy.check.others` - Permite al usuario ver el balance de otro jugador en una economía.
- `globalmultieconomy.economy.check.*` - Permite al usuario ver el balance de cualquier jugador en cualquier economía.
- `globalmultieconomy.economy.admin` - Permite al usuario añadir, quitar y establecer el balance de cualquier jugador en
  cualquier economía.
- `globalmultieconomy.economy.*` - Permite al usuario hacer cualquier cosa relacionada con las economías.
- `globalmultieconomy.*` - Permite al usuario hacer cualquier cosa relacionada con el plugin.

## Soporte

Si tienes cualquier duda, fallo o sugerencia házmelo saber en mi página
de [SpigotMC](https://www.spigotmc.org/members/adrian_oliva.113464/), o en mi
discord [Adrian_oliva](https://discord.com/users/349718498827698181)