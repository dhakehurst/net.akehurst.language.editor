<template>
  <div id='element' class="agl-editor-ace-vue">
  </div>
</template>

<script>
import agl from 'net.akehurst.language.editor-agl-editor-ace/net.akehurst.language.editor-agl-editor-ace.js'
const AglEditorAce = agl.net.akehurst.language.editor.ace.AglEditorAce;
//const ParseEventStart = agl.net.akehurst.language.editor.api.ParseEventStart;
//const ParseEventSuccess = agl.net.akehurst.language.editor.api.ParseEventSuccess;
//const ParseEventFailure = agl.net.akehurst.language.editor.api.ParseEventFailure;

const element = document.getElementById("element");
const languageId = 'test';
const editorId = 'editor1';
const options = {
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: false
};
new AglEditorAce(element, languageId, editorId, options, 'worker.js');

export default {
  name: 'agl-editor-ace',
  props: {
    msg: String
  }
}
</script>


<style scoped>
.agl-editor-ace-vue {
  display: flex;
  flex: 1;
}
</style>
