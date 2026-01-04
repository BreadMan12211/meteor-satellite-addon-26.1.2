const biomeDimensionSelect = document.getElementById("biome-dimension-select");
const mapGotoX = document.getElementById("map-goto-x");
const mapGotoZ = document.getElementById("map-goto-z");
const goButton = document.getElementById("map-goto-go");

let socket;

function connect() {
    try {
        socket = new WebSocket('ws://localhost:%s');
    } catch (e) {
        console.error("WebSocket connection failed:", e);
    }

    socket.onopen = function(event) {
        console.log("Connected to server");
    };

    socket.onmessage = function (event) {
        const message = event.data;
        const packet = JSON.parse(message);
        const { x, z, dimension } = packet;

        if (x) {
            mapGotoX.value = x;
        }
        if (z) {
            mapGotoZ.value = z;
        }
        if (dimension) {
            biomeDimensionSelect.value = dimension;
            biomeDimensionSelect.dispatchEvent(new Event("change"))
        }

        if (x || z) {
            goButton.click();
        }
    };

    socket.onclose = function (event) {
        console.log("WebSocket closed, reconnecting in 2 seconds...");
        if (socket && socket.readyState === WebSocket.OPEN)
            return;
        setTimeout(connect, 2000);
    };

    socket.onerror = function (event) {
        console.error("WebSocket error observed:", event);
        socket.close();
    }
}

connect();
