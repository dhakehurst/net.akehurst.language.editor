'use strict';

import './agl-ace.css';
import agl_editor_ace from './net.akehurst.language.editor-agl-editor-ace.js';
const AglEditorAce = agl_editor_ace.net.akehurst.language.editor.ace.AglEditorAce;

class AglEditorAceWebComponent extends HTMLElement {

  static get observedAttributes() {
    return ['languageId', 'editorId', 'options', 'workerScript'];
  }

  constructor() {
    super();
    this.aglAceEditor = null;
    if (!this.editorId) this.editorId = this.getAttribute('id');
    if (!this.languageId) this.languageId = this.editorId;
    if (!this.options) this.options = '{}';
    if (!this.workerScript) this.workerScript = 'net.akehurst.language.editor-agl-editor-worker.js';
  }

  connectedCallback() {
    const shadowRoot = this.attachShadow({mode: 'open'});
    const element = document.createElement('div');
    shadowRoot.appendChild(element);
    shadowRoot.style='display: grid; grid: auto-flow minmax(0, 1fr) / minmax(0, 1fr);';
    element.style='height:100%';
    const options = JSON.parse(this.options);
    this.aglAceEditor = new AglEditorAce(element, this.languageId, this.editorId, options, this.workerScript);
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

  get options() { return this.getAttribute('options'); }
  set options(newValue) { if(newValue) this.setAttribute('options', newValue); else this.removeAttribute('options'); }

  get workerScript() { return this.getAttribute('workerScript'); }
  set workerScript(newValue) { if(newValue) this.setAttribute('workerScript', newValue); else this.removeAttribute('workerScript'); }

  get text() { return this.aglAceEditor.text; }
  set text(newValue) { if(newValue) this.setAttribute('text', newValue); else this.removeAttribute('text'); }

  get grammarStr() { return this.aglAceEditor.grammarStr; }
  set grammarStr(newValue) { this.aglAceEditor.grammarStr = newValue; }

  get styleStr() { return this.aglAceEditor.styleStr; }
  set styleStr(newValue) { this.aglAceEditor.styleStr = newValue; }
}

customElements.define('agl-editor-ace', AglEditorAceWebComponent);
