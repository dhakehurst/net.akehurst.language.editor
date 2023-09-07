requirejs(['./net.akehurst.language.editor-browser-agl.js'], function(agl_ed) {
    //import './net.akehurst.language.editor-browser-agl.js';
    const AglEditorDefault = agl_ed.net.akehurst.language.editor.browser.agl.AglEditorDefault;

    let el = document.querySelector("#editor")
    let ed = new AglEditorDefault(el, "test", "test", "./worker.js", true)
    ed.logger.bind = (lvl, msg, t) => { console.log(lvl, msg, t) }
    ed.languageDefinition.grammarStr = `
    namespace test
    grammar Test {
        skip leaf WS = "\s+" ;
        sentence = 'hello' 'world' ;
    }
    `

});