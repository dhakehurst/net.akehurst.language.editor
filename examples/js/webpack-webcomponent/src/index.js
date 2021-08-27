'use strict';

import 'net.akehurst.language.editor-agl-editor-ace/webcomponent.js'

import './index.css'
import agl from 'net.akehurst.language.editor-agl-editor-ace/net.akehurst.language.editor-agl-editor-ace.js'
const AglLanguage = agl.net.akehurst.language.api.processor.AglLanguage;
const AglEditorAce = agl.net.akehurst.language.editor.ace.AglEditorAce;
const ParseEventStart = agl.net.akehurst.language.editor.api.ParseEventStart;
const ParseEventSuccess = agl.net.akehurst.language.editor.api.ParseEventSuccess;
const ParseEventFailure = agl.net.akehurst.language.editor.api.ParseEventFailure;


function toString(node, indent) {
    var str = '';
    
    if (node.isBranch) {
        str += indent + node.name + ' {\n';
        for(let child of node.children) {
            str+= toString(child, indent+'  ') + '\n';
        }
        str += indent + '}';
    } if (node.isEmpty) {
        str += indent + '$empty';
    } else {
        str += indent + "'"+node.nonSkipMatchedText+"'";
    }
    
    return str;
}

const grammarEditor = document.getElementById("grammarEditor");
const sentenceEditor = document.getElementById("sentenceEditor");
//const sentenceEl = document.getElementById("sentenceEditor");
//const sentenceEditor = new AglEditorAce(sentenceEl);
const result = document.getElementById("result");

grammarEditor.options = `{
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
}`;
grammarEditor.grammarStr = '@Agl.grammarProcessor@';
grammarEditor.styleStr = AglLanguage.grammar.style;
grammarEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        sentenceEditor.grammarStr = null;
    } else if (e instanceof ParseEventSuccess) {
        sentenceEditor.grammarStr = grammarEditor.text;
    }
});
grammarEditor.text = `namespace test
grammar Test {
    skip WS = "\\s+" ;
    greeting = 'Hello' 'World!' ;
}
`;


styleEditor.options = `{
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
}`;
styleEditor.grammarStr = '@Agl.styleProcessor@';
styleEditor.styleStr = AglLanguage.style.style;
styleEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        sentenceEditor.styleStr = null;
    } else if (e instanceof ParseEventSuccess) {
        sentenceEditor.styleStr = styleEditor.text;
    }
});
styleEditor.text = `$keyword {
    foreground: blue;
    font-style: bold;
}`;


sentenceEditor.options = `{
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
}`;
sentenceEditor.grammarStr = grammarEditor.text;
sentenceEditor.styleStr = styleEditor.text;
sentenceEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        result.value = e.message;
    } else if (e instanceof ParseEventSuccess) {
        result.value = toString(e.tree, '');
    }
});
sentenceEditor.text = 'Hello World!';


