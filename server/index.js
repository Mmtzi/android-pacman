var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];
var consumableobjects = [];
var ghostobjects = [];
var playersInLobby = [];

server.listen(8080, function (){
    console.log("Server is now running...");
});

io.on('connection', function (socket){
    console.log("Player Connected!");
    socket.emit('socketID', {id: socket.id});

    socket.on('playerMoved', function(data){
        socket.broadcast.emit('playerMoved', data);
        console.log("Player moved: "+ data.id + " X: "+ data.x+ " Y: "+data.y + " Points: "+data.p+ " Deaths: "+data.deaths);
        for(var i = 0; i< players.length; i++) {
            if (players[i].id == data.id) {
                players[i].x = data.x;
                players[i].y = data.y;
                players[i].points = data.p;
                players[i].deaths = data.deaths;
            }
        }
    });

    socket.on('disconnect', function(){
        console.log("Player "+socket.id+" disconnected");
        for(var i = 0; i<players.length; i++) {
            if (players[i].socketID == socket.id) {
                console.log(players[i].id);
                socket.broadcast.emit('playerDisconnected', players[i].id);
                players.splice(i,1)
                if (players[i].id == 1) {
                    console.log("clearArrays");
                    players = [];
                    consumableobjects =[];
                    playersInLobby = [];
                    ghostobjects =[];
                }
            }
        }
    });
    socket.on('updateConsumables', function(data) {
        socket.broadcast.emit('updateConsumables', data)
        for (var i = 0; i<consumableobjects.length; i++) {
        if (consumableobjects[i] = data) {
            console.log("delete Consumable:" + data.index)
            consumableobjects.splice(i,1)
            }
        }
    });

    socket.on('createInteractiveMapObjects', function(data) {
    myConsumableList = data.consumableList;
    myGhostList = data.ghostList;
    if(players.length == 1) {
        for (var i = 0; i<myConsumableList.length; i++) {
            consumableobjects.push(myConsumableList[i])
            console.log("add Consumable"+i+" to List")
        }
        for (var i = 0; i<myGhostList.length; i++) {}
             ghostobjects.push(myGhostList[i])
             console.log("add Ghost"+i+" to List")
        }
    });
    socket.on('createPlayer', function(data) {
        socket.broadcast.emit('newPlayer', data);
    });

    socket.on('ghostMoved', function(data){
        socket.broadcast.emit('ghostMoved', data);
        //console.log("Ghost moved: "+ data.id + " X: "+ data.x+ " Y: "+data.y);
        for(var i = 0; i< ghostobjects.length; i++) {
        if (ghostobjects[i].id == data.id) {
            ghostobjects[i].x = data.x;
            ghostobjects[i].y = data.y;
            }
        }
    });
    socket.on('getPlayerCountLobby', function() {
        socket.emit('getPlayersLobby', playersInLobby);
    });
    socket.on('createPlayerInLobby', function() {
        playersInLobby.push(true);
        socket.broadcast.emit('newPlayerLobby', {id: socket.id});
    });
    socket.on('startGame', function(data) {
        socket.broadcast.emit('startGame', data.map);
        players.push(new player(socket.id, data.id, 210+30*(data.id-1),840, 0,0))
    });
    socket.on('endGame', function() {
        socket.broadcast.emit('endGame');
        ghostobjects = [];
        playersInLobby = [];
        players= [];
        consumableobjects =[];
    })
});

function player(socketID, id, x, y, points, deaths) {
    this.socketID = socketID;
    this.id = id
    this.x = x
    this.y = y
    this.points = points
    this.deaths = deaths
}

function ghost(id, x, y) {
    this.id = id
    this.x = x
    this.y = y
}