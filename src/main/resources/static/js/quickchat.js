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
    .then(res => {
        usernameField.value = res[0];
        username = res[0]
    })
    .catch((error) => {
        username = 'Tom';
        usernameField.value = 'Tom';
    });

/*****       hook up chat input field     ******/
document.getElementById('chat-form').onsubmit = function (submitEvent) {
    submitEvent.preventDefault();
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
    console.log("Message received!", message);
    chat.appendChild(createMessageBubble(message));
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
    console.log("Some Error occured: ", error);
}
setInterval(() => {
    chat.querySelectorAll('[data-time-raw]')
        .forEach((elem) => updateTimeText(elem));
},30000);

/*****       Utility functions      ******/

function stripHTML(html){
    let doc = new DOMParser().parseFromString(html, 'text/html');
    return doc.body.textContent || "";
}

function createMessageBubble(message) {
    const messageOuter = document.createElement('li');
    messageOuter.classList.add('chat__message-wrapper');

    const messageInner = document.createElement('span');
    messageInner.classList.add('chat__message');
    if(message.username === username) {
        messageInner.classList.add('own');
    }
    messageInner.innerHTML = message.username + ': ' + stripHTML(message.text);

    const timeHint = document.createElement('span');
    timeHint.classList.add('chat__message-time');
    timeHint.dataset.timeRaw = message.id.date;
    updateTimeText(timeHint);

    messageInner.appendChild(timeHint);
    messageOuter.appendChild(messageInner);
    return messageOuter;
}

function date2ChatTime(date) { //https://plnkr.co/edit/mUc0gvvIUZRqoY27?p=preview&preview
    let diff = new Date() - date; // the difference in milliseconds

    if (diff < 1000) { // less than 1 second
        return 'right now';
    }

    let sec = Math.floor(diff / 1000); // convert diff to seconds

    if (sec < 60) {
        return sec + ' sec. ago';
    }

    let min = Math.floor(diff / 60000); // convert diff to minutes
    if (min < 60) {
        return min + ' min. ago';
    }

    // format the date
    // add leading zeroes to single-digit day/month/hours/minutes
    let d = date;
    d = [
        '0' + d.getDate(),
        '0' + (d.getMonth() + 1),
        '' + d.getFullYear(),
        '0' + d.getHours(),
        '0' + d.getMinutes()
    ].map(component => component.slice(-2)); // take last 2 digits of every component

    // join the components into date
    return d.slice(0, 3).join('.') + ' ' + d.slice(3).join(':');
}
const updateTimeText = timeElement => {
    timeElement.innerHTML = date2ChatTime(new Date(Date.parse(timeElement.dataset.timeRaw)));
}
