'use strict';

import 'net.akehurst.language.editor-agl-editor-ace/webcomponent.js'

import './index.css'
import agl from 'net.akehurst.language.editor-agl-editor-ace/net.akehurst.language.editor-agl-editor-ace.js'
const Agl = agl.net.akehurst.language.agl.processor.Agl;
const AglLanguage = agl.net.akehurst.language.api.processor.AglLanguage;
const AglEditorAce = agl.net.akehurst.language.editor.ace.AglEditorAce;
const ParseEventStart = agl.net.akehurst.language.editor.api.ParseEventStart;
const ParseEventSuccess = agl.net.akehurst.language.editor.api.ParseEventSuccess;
const ParseEventFailure = agl.net.akehurst.language.editor.api.ParseEventFailure;

const sentenceEditorId = 'sentenceEditor';
const sentenceEditor = document.getElementById(sentenceEditorId);
const grammarEditor = document.getElementById("grammarEditor");
const styleEditor = document.getElementById("styleEditor");
const result = document.getElementById("result");

// kotlin IR/JS bug, names not correct!
grammarEditor.languageId = Agl.registry.agl._grammarLanguageIdentity;
styleEditor.languageId = Agl.registry.agl._styleLanguageIdentity;

// find sentenceEditor language definition, so that editor can reference it
const userLang = Agl.registry.findOrPlaceholder(sentenceEditorId);


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


grammarEditor.options = {
    editor: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: false
    }
};
grammarEditor.text = `namespace test
grammar Test {
    skip WS = "\\s+" ;
    greeting = 'Hello' 'World!' ;
}
`;

styleEditor.options = {
    editor: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: false
    }
};
styleEditor.text = `$keyword {
    foreground: blue;
    font-style: bold;
}`;

grammarEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        userLang.grammar = null;
    } else if (e instanceof ParseEventSuccess) {
        userLang.grammar = grammarEditor.text;
    }
});

styleEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        userLang.style = null;
    } else if (e instanceof ParseEventSuccess) {
        userLang.style = styleEditor.text;
    }
});




sentenceEditor.options = {
    editor: {
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: false
    }
};

sentenceEditor.onParse( (e) =>{
    if (e instanceof ParseEventFailure) {
        result.value = e.message;
    } else if (e instanceof ParseEventSuccess) {
        result.value = toString(e.tree, '');
    }
});
userLang.grammar = grammarEditor.text
userLang.style = styleEditor.text
sentenceEditor.text = 'Hello World!';


