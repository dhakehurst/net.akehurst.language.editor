'use strict';

import './ace.css'
import './index.css'
import agl from '../node_modules/net.akehurst.language.editor-agl-editor-ace/net.akehurst.language.editor-agl-editor-ace.js'
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
    } else {
        str += indent + "'"+node.nonSkipMatchedText+"'";
    }
    
    return str;
}



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

const result = document.getElementById("result");


grammarEditor.onblur = ()=>sentenceEditor.setProcessor(grammarEditor.value);
grammarEditor.grammarStr = '@Agl.grammarProcessor@';
grammarEditor.styleStr = AglLanguage.grammar.style;
grammarEditor.value = `
namespace test
grammar Test {
    skip WS = "\\s+" ;
    greeting = 'Hello' 'World!' ;
}
`;

sentenceEditor.options = `
{
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
}`;
sentenceEditor.grammarStr = grammarEditor.text;
sentenceEditor.styleStr = `
$keyword {
    foreground: blue;
    font-style: bold;
}
`;
sentenceEditor.text = 'Hello World!';

sentenceEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        result.value = e.message;
    } else if (e instanceof ParseEventSuccess) {
        result.value = toString(e.tree, '');
    }
});
