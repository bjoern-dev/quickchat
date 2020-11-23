let username = '';
const usernameField = document.getElementById('myusername');

/*****      Get username from name-API        ********/
fetch('https://namey.muffinlabs.com/name.json?count=1&with_surname=false&frequency=common', {
    method: 'get',
    headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json'
    }
})
    .then(res=>res.json())
    .then(res => {console.log(res);
        usernameField.value = res[0];
        username = res[0]
    })
    .catch((error) => {
        username = 'Tom';
        usernameField.value = 'Tom';
    });

/*****       hook up chat input field     ******/
document.getElementById('chat-form').onsubmit = function (evt) {
    evt.preventDefault();
    const inputField = document.getElementById('chat-input');
    let chatMessage = stripHTML(inputField.value);
    inputField.value = '';

    fetch('/messages/', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({username: username, text: chatMessage})
    })
        .then(res=>res.json())
        .then(res => console.log('Sent a message and got this response from server: ', res));
}

/*****       Message Display, constantly reading from stream      ******/

const chat = document.getElementById('messages');
const chatScrollBox = document.getElementById('chat__area');
const eventSource = new EventSource("/messages/sse-endpoint/");

eventSource.onmessage = function(event) {
    const message = JSON.parse(event.data);
    const messageOuter = document.createElement('li');
    const messageInner = document.createElement('span');
    console.log("Message received!", message);
    messageInner.classList.add('chat__message');
    if(message.username === username) {
        messageInner.classList.add('own');
    }
    messageInner.innerHTML = message.username + ': ' + stripHTML(message.text);
    messageOuter.appendChild(messageInner);
    chat.appendChild(messageOuter);
    chatScrollBox.scrollTop = chatScrollBox.scrollHeight;
};
eventSource.addEventListener('heartbeat', event => {
    console.log("Heartbeat: ", JSON.parse(event.data).comment);
});
eventSource.onerror = function(error) {
    console.log("State: ", eventSource.readyState);
    if(eventSource.readyState === EventSource.CLOSED) {
        console.log("Connection has been closed");
    }
    console.log("Some Error occured: " + error);
    console.log(error);

}

function stripHTML(html){
    let doc = new DOMParser().parseFromString(html, 'text/html');
    return doc.body.textContent || "";
}
