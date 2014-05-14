#######################################
#######################################
##                                   ##
##          NiKate - Origins         ##
##               v1.0                ##
##                                   ##
#######################################
#######################################
by Benito Palacios Sánchez, aka pleonex


|-> Game objectives
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
NiKate is a simple MMORPG game written
in Java. The objetive is to get 15
experience points.

Each point is obtains by being over
another player (in the same position).
This will give you one point.

Furthermore, each player has Health
Points. You can lost them when another
player attack you. When you attack,
it will appear some fire near to you,
if the fire hit another player it
will lost 4 HP. Here is a figure of
the fire position.

X -> Player hit by the fire.
O -> Our player
# -> Normal position

X  #  X
#  O  #
X  #  X

The attack will take 1 second.

There are 4 maps. Each of them can
host 8 players at the same time.
The server will decide the map of each
player when they log in the service.
The assignment will be incremental,
starting by the map 0. Moreover, it
avoid assigning a map where there is a
user with the same ID (anti-spoofing).


|-> Controls
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Movement
 * W or up arrow: Up movement
 * S or down arrow: Down movement
 * A or left arrow: Left movement
 * D or right arrow: Right movement
 
Attack
 * Space bar

 
|-> Requirements
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
At the start of the game, you must
click in the map to give the focus to
the component and be able to move the
player.

The game needs the "res" folder with
the images and sound files of the game.
This folder is not distributed with the
code. It must contain the following
files:

./res/
./res/ALDEANO.png -> ALDEANO char
./res/DIOS.png    -> DIOS char
./res/MAGO.png    -> MAGO char
./res/SABIO.png   -> SABIO char
./res/fuego.png   -> attack image
./res/mapa0.png   -> map 0
./res/mapa1.png   -> map 1
./res/mapa2.png   -> map 2
./res/mapa3.png   -> map 3
./res/inicio.wav  -> start audio
./res/main_inicio.wav -> main bg audio
./res/main_loop.wav -> loop bg audio
        
The char and attack images must be
40x40 pixels. The map images must be
400x400 pixels.

To run the clien use the following line
    java -jar NiKate_-_Origins.jar
This will execute the main method in
the IniciaSesion class.

To execute the server you be in the
class folder and use the following line
    java servidor/Servidor 9090
where 9090 is the port to listen.