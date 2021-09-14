'use strict';

import agl_ace_css from '!!css-loader!./agl-ace.css';
import agl_editor_ace from './net.akehurst.language.editor-agl-editor-ace.js';
const AglEditorAce = agl_editor_ace.net.akehurst.language.editor.ace.AglEditorAce;

class AglEditorAceWebComponent extends HTMLElement {

  static get observedAttributes() {
    return ['languageId', 'editorId', 'options', 'workerScript', 'sharedWorker', 'grammarStr', 'styleStr'];
  }

  aglEditor = null;
  options = {};
  _initialised = false;

  constructor() {
    super();
  }

  connectedCallback() {
    if (this._initialised) {
      // nothing
    } else {
      if (!this.editorId) this.editorId = this.getAttribute('id');
      if (!this.languageId) this.languageId = this.editorId;
      if (!this.workerScript) this.workerScript = 'net.akehurst.language.editor-agl-editor-worker.js';
      if (this.hasAttribute('options')) this.options = JSON.parse(this.getAttribute('options'));
      if (this.hasAttribute('grammarStr')) this.grammarStr = this.getAttribute('grammarStr');
      if (this.hasAttribute('styleStr')) this.styleStr = this.getAttribute('styleStr');
      const shadowRoot = this.attachShadow({mode: 'open'});
      const style = document.createElement('style');
      shadowRoot.appendChild(style);
      style.textContent = `:host {
    display: flex;
    min-height: 1em;
    flex-direction: column;
}
#editor_div {
  flex: 1;
  height:100%;
}
`;

      const edStyle = document.createElement('style');
      edStyle.innerHTML = agl_ace_css;
      shadowRoot.appendChild(edStyle);

      const element = document.createElement('div');
      element.id = 'editor_div';
      shadowRoot.appendChild(element);
      this.aglEditor = new AglEditorAce(element, this.languageId, this.editorId, this.options, this.workerScript, this.sharedWorker);
      this.aglEditor.aceEditor.renderer.attachToShadowRoot();
      this._initialised = true;
    }
  }

  disconnectedCallback() {
    this.aglEditor.finalize();
  }

  attributeChangedCallback(attrName, oldValue, newValue) {
    if (newValue !== oldValue) {
      this[attrName] = newValue;
    }
  }

  get languageId() { return this.getAttribute('languageId'); }
  set languageId(newValue) { if(newValue) this.setAttribute('languageId', newValue); else this.removeAttribute('languageId'); }

  get editorId() { return this.getAttribute('editorId'); }
  set editorId(newValue) { if(newValue) this.setAttribute('editorId', newValue); else this.removeAttribute('editorId'); }

  get workerScript() { return this.getAttribute('workerScript'); }
  set workerScript(newValue) { if(newValue) this.setAttribute('workerScript', newValue); else this.removeAttribute('workerScript'); }

  get sharedWorker() { return this.hasAttribute('sharedWorker'); }
  set sharedWorker(newValue) { if(newValue) this.setAttribute('sharedWorker', newValue); else this.removeAttribute('sharedWorker'); }


  get text() { return this.aglEditor.text; }
  set text(newValue) {
    if(typeof(newValue)==='string') {
      this.aglEditor.text = newValue;
    } else {
      this.aglEditor.text = '';
    }
  }

  get grammarStr() { return this.aglEditor.grammarStr; }
  set grammarStr(newValue) {
    if(typeof(newValue)==='string') {
      this.aglEditor.grammarStr = newValue;
    } else {
      this.aglEditor.grammarStr = null;
    }
  }

  get styleStr() { return this.aglEditor.styleStr; }
  set styleStr(newValue) {
    if(typeof(newValue)==='string') {
      this.aglEditor.styleStr = newValue;
    } else {
      this.aglEditor.styleStr = null;
    }
  }

  clearErrorMarkers() { this.aglEditor.clearErrorMarkers(); }

  onParse(handler) { this.aglEditor.onParse(handler); }

  onProcess(handler) { this.aglEditor.onProcess(handler); }

}

customElements.define('agl-editor-ace', AglEditorAceWebComponent);
