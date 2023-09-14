importScripts("./require.min.js")
var w;
requirejs(['./net.akehurst.language.editor-agl-editor-browser-worker.js'], function(alg_worker) {
    const AglSharedWorker = alg_worker.net.akehurst.language.editor.worker.AglSharedWorker;
    w = new AglSharedWorker()
})