const { ipcRenderer } = require('electron');

process.once('loaded', () => {
    window.addEventListener('message', event => {
        // do something with custom event
        const message = event.data;

        if (message.type === 'exit') {
            ipcRenderer.send('custom-message', message);
        }
    });
});
