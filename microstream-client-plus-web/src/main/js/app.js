import postcss from "postcss";

const required = require;
const $ = required("jquery");
window.jQuery = $;
window.$ = $;
import "bootstrap";
import "../sass/app.scss";
import Chart from 'chart.js';
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api';
import 'monaco-editor/esm/vs/basic-languages/java/java.contribution.js';
const jsonmarkup = require('json-markup')

$(document).ready(function () {
    const chartElement = document.getElementsByClassName('chart');
    Array.from(chartElement).forEach((el) => {
        if (el) {
            const config = el.dataset.config;

            new Chart(
                el,
                JSON.parse(config)
            );
            console.log("chart ready!");
        }
    });

    document.buildConsole = buildConsole;

});

function buildConsole(editorId, outputId) {
    const monacoElement = document.getElementById(editorId);
    if (monacoElement) {
        //Autocompletion
        monaco.languages.registerCompletionItemProvider('java', {
            provideCompletionItems: async function (model, position) {
                // find out if we are completing a property in the 'dependencies' object.
                const textUntilPosition = model.getValueInRange({
                    startLineNumber: 1,
                    startColumn: 1,
                    endLineNumber: position.lineNumber,
                    endColumn: position.column
                });

                const word = model.getWordUntilPosition(position);
                const range = {
                    startLineNumber: position.lineNumber,
                    endLineNumber: position.lineNumber,
                    startColumn: word.startColumn,
                    endColumn: word.endColumn
                };
                return {
                    suggestions: await completions(textUntilPosition, range)
                };
            }
        });

        //editor
        document.monaco = monaco.editor.create(monacoElement, {
            value: ``,
            language: 'java',
            theme: 'vs-dark'
        });

        //change content
        document.monaco.getModel().onDidChangeContent(() => {
            const content = document.monaco.getModel().getValue();
            const selectedContent = document.monaco.getModel().getValueInRange(document.monaco.getSelection());
            console.log(content);
            console.log(selectedContent);
        });

        //output
        const outputElement = document.getElementById(outputId);
        document.monaco.outputElement = outputElement;

    }
    console.log("console ready!");

    document.monaco.invoke = invoke;
    document.monaco.getRange = getRange;
    document.monaco.markError = markError;
    document.monaco.output = output;
    document.close = close;
}

function close() {
    //electron
    if (window.postMessage) {
        window.postMessage({
            type: 'exit',
        });
    }
}

async function invoke() {
    let content = "";
    const selectedContent = document.monaco.getModel().getValueInRange(document.monaco.getSelection());
    const fullContent = document.monaco.getModel().getValue();
    if (selectedContent) {
        content = selectedContent;
    } else {
        content = fullContent;
    }

    const contentBase64 = encodeURI(content);
    const response = await fetch(`/terminal/invoke?content=${contentBase64}`)
    const result = await response.json();

    document.monaco.markError(result);
    console.log(result);

    document.monaco.output(result);
}

function output(result) {
    const output = result.output;
    const errors = result.errors;
    $(document.monaco.outputElement).children().detach();

    for (const item of output) {
        let html = "";
        try {
            html = jsonmarkup(JSON.parse(item));
        }
        catch (e)
        {
            html = item;
        }
        document.monaco.outputElement.insertAdjacentHTML(
            'beforeend',
            `<div style="color: white">${html}</div>`,
        );
    }

    for (const error of errors) {
        const message = error.message;
        document.monaco.outputElement.insertAdjacentHTML(
            'beforeend',
            `<div style="color: red">${message}</div>`,
        );
    }
}

async function completions(content, range) {
    const contentBase64 = encodeURI(content);
    const response = await fetch(`/terminal/suggest?content=${contentBase64}`)
    const result = await response.json();
    console.log(result);
    return result;
}

function markError(output) {
    const errors = output.errors;
    const markers = [];
    for (const error of errors) {
        const range = document.monaco.getRange(error.startPosition, error.endPosition);
        const marker = {
            message: error.message,
            severity: monaco.MarkerSeverity.Error,
            startLineNumber: range.startLineNumber,
            startColumn: range.startColumn,
            endLineNumber: range.endLineNumber,
            endColumn: range.endColumn
        };
        markers.push(marker);
    }
    monaco.editor.setModelMarkers(document.monaco.getModel(), 'owner', markers);
}

function getRange(startPosition, endPosition) {
    const lines = document.monaco.getModel().getLineCount();
    const range = {
        startLineNumber: 0,
        endLineNumber: 0,
        startColumn: 0,
        endColumn: 0
    };

    let currentCharPosition = 1;
    let currentLine = 1;
    for (let i = 1; i < lines + 1; i++) {
        const lineLength = document.monaco.getModel().getLineContent(currentLine).length;
        if ((startPosition - (currentCharPosition + lineLength)) < 0 && range.startLineNumber === 0) {
            range.startLineNumber = currentLine;
            range.startColumn = startPosition - currentCharPosition + 2;
        }

        if ((endPosition - (currentCharPosition + lineLength)) < 0 && range.endLineNumber === 0) {
            range.endLineNumber = currentLine;
            range.endColumn = endPosition - currentCharPosition + 2;
        }
        currentCharPosition = currentCharPosition + lineLength + 1;
        currentLine++;
    }
    if (range.startColumn === range.endColumn && range.startLineNumber === range.endLineNumber) {
        range.endColumn = range.endColumn + 1;
    }
    return range;
}
