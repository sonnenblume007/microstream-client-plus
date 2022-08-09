const path = require('path')
const fs = require('fs');
const {app, BrowserWindow, ipcMain, dialog} = require('electron');
const fetch = require('cross-fetch');
const killProcessByName = require('kill-process-by-name');
const { kill } = require('cross-port-killer');

const createWindow = async () => {
    console.log('Start application ' + app.name);

    const errorList = [];
    const port = '8082';
    const win = new BrowserWindow({
        autoHideMenuBar: true,
        minWidth: 1400,
        width: 1400,
        minHeight: 1100,
        title: '',
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
        }
    });

    win.loadFile('index.html')
    //Communicate with browser
    ipcMain.on('custom-message', (event, message) => {
        if (message.type === 'exit') {
            console.log('got an IPC message', event, message);
            win.close();
        }
    });

    //Kill port
    console.log(`Kill port ${port}`);
    await killPort(port);

    //Find Jar
    let filePath = await findJarFile(getRootPath());
    console.log(`Jar found ${filePath}.`);

    //Start backend
    if (filePath) {
        console.log('Start backend.');
        startBackend(filePath, (error) => {

        });
        console.log('Backend started.');
    }

    //Kill backend
    win.on('closed', function () {
        killPort(port);
        killProcessByName(app.name);
    });

    //Load content
    console.log('Load content.');
    await loadContent(port, errorList, win);
};

async function killPort(port) {
    try {
        await kill(port);
    } catch (e) {
        console.log(e);
    }
}

function findJarFile(rootPath) {
    return new Promise((resolve, reject) => {
        fs.readdir(rootPath, function (err, files) {
            //listing all files using forEach
            files.forEach(function (file) {
                // Do whatever you want to do with the file
                if (file.match('.jar')) {
                    resolve(rootPath + file);
                }
            });
            resolve('');
        });
    });
}

function startBackend(filepath, errorCallback) {
    const spawn = require('child_process')
    const child = spawn.exec(`java -jar ${filepath}`, function (error, stdout, stderr) {
        if (error) {
            console.error('StartBackend error');
            console.error(error);
            errorCallback(error)
        } else if (stderr) {
            console.error('StartBackend stderr');
            console.error(stderr);
            errorCallback(stderr)
        } else if (stdout) {
            console.error('StartBackend stdout');
            console.error(stdout);
        }
    });
    return child.pid;
}

async function loadContent(port, errorList, win, limit = 20) {
    const url = `http://localhost:${port}/terminal`;

    if (errorList.length > 0) {
        sendErrorMessage(errorList.join('<br>'), win);
        return;
    }

    try {
        const response = await fetch(url);
        if (response) {
            await win.loadURL(url);
        }
    } catch (e) {
        if (limit === 0) {
            errorList.push(
                '<span>Could not start the java application.</span><br>' +
                '<span style="font-size: smaller; font-weight: normal;">Java Version 11 required.</span><br>' +
                '<span style="font-size: smaller; font-weight: normal;">Start the application with the command line for more information.</span>' +
                '');
        }
        limit -= 1;
        console.log(`Wait... ${limit}`);
        setTimeout(async function () {
            await loadContent(port, errorList, win, limit);
        }, 500);
    }
}

function sendErrorMessage(content, win) {
    win.webContents.executeJavaScript('' +
        'const messageBox = document.getElementById(\'messageBox\');\n' +
        'messageBox.innerHTML = \'<span>' + content + '</span>\';\n' +
        'const spinner = document.getElementById(\'spinner\');\n' +
        'spinner.style=\'display:none;\'\n' +
        'const warning = document.getElementById(\'warning\');\n' +
        'warning.style=\'display:block;font-size: 100px;\'\n' +
        ''
    );
}

function getRootPath() {
    console.log('Root-Path');
    const appPath = app.getAppPath() ? app.getAppPath() : app.getPath('userData');
    let extension = appPath.substr(appPath.lastIndexOf('.') + 1);
    let filepath;
    console.log(appPath);
    if (extension !== 'asar' && appPath.match('/')) {
        //Linux & Mac
        filepath = appPath + '/jar/';
    } else if (extension !== 'asar' && appPath.match('\\')) {
        //Windows
        filepath = appPath + '\\jar\\';
    } else if (appPath.match('/')) {
        //Linux & Mac
        filepath = appPath.substring(0, appPath.lastIndexOf('/')) + '/jar/';
    } else {
        //Windows
        filepath = appPath.substring(0, appPath.lastIndexOf('\\')) + '\\jar\\';
    }
    return filepath;
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

