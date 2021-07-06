import './ace.css'
import './index.css'
import agl from '../node_modules/net.akehurst.language.editor-agl-editor-ace/net.akehurst.language.editor-agl-editor-ace.js'
const AglEditorAce = agl.net.akehurst.language.editor.ace.AglEditorAce;


const grammarEditor = document.getElementById("grammarEditor");

const element = document.getElementById("sentenceEditor");
const languageId = 'test';
const editorId = 'editor1';
const options = {
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
};
const sentenceEditor = new AglEditorAce(element, languageId, editorId, options, 'worker.js');
sentenceEditor.setProcessor(grammarEditor.value);
