'use strict';

import agl_ace_css from '!!css-loader!./agl-ace.css';
import agl_editor_ace from './net.akehurst.language.editor-agl-editor-ace.js';
const AglEditorAce = agl_editor_ace.net.akehurst.language.editor.ace.AglEditorAce;

class AglEditorAceWebComponent extends HTMLElement {

  static get observedAttributes() {
    return ['languageId', 'editorId', 'options', 'workerScript', 'grammarStr', 'styleStr'];
  }

  _initialised = false;

  constructor() {
    super();
    this.aglAceEditor = null;
  }

  connectedCallback() {
    if (this._initialised) {
      // nothing
    } else {
      if (!this.editorId) this.editorId = this.getAttribute('id');
      if (!this.languageId) this.languageId = this.editorId;
      if (!this.options) this.options = '{}';
      if (!this.workerScript) this.workerScript = 'net.akehurst.language.editor-agl-editor-worker.js';
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
#ace_div {
  flex: 1;
  height:100%;
}
`;

      const aceStyle = document.createElement('style');
      aceStyle.innerHTML = agl_ace_css;
      shadowRoot.appendChild(aceStyle);

      const element = document.createElement('div');
      element.id = 'ace_div';
      shadowRoot.appendChild(element);
      const options = JSON.parse(this.options);
      this.aglAceEditor = new AglEditorAce(element, this.languageId, this.editorId, options, this.workerScript);
      this.aglAceEditor.aceEditor.renderer.attachToShadowRoot();
      this._initialised = true;
    }
  }

  disconnectedCallback() {
    this.aglAceEditor.finalize();
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
  set text(newValue) {
    if(typeof(newValue)==='string') {
      this.aglAceEditor.text = newValue;
    } else {
      this.aglAceEditor.text = '';
    }
  }

  get grammarStr() { return this.aglAceEditor.grammarStr; }
  set grammarStr(newValue) {
    if(typeof(newValue)==='string') {
      //this.setAttribute('grammarStr', newValue);
      this.aglAceEditor.grammarStr = newValue;
    } else {
      //this.removeAttribute('grammarStr');
      this.aglAceEditor.grammarStr = null;
    }
  }

  get styleStr() { return this.aglAceEditor.styleStr; }
  set styleStr(newValue) {
    if(typeof(newValue)==='string') {
      //this.setAttribute('styleStr', newValue);
      this.aglAceEditor.styleStr = newValue;
    } else {
      //this.removeAttribute('styleStr');
      this.aglAceEditor.styleStr = null;
    }
  }

  clearErrorMarkers() { this.aglAceEditor.clearErrorMarkers(); }

  onParse(handler) { this.aglAceEditor.onParse(handler); }

  onProcess(handler) { this.aglAceEditor.onProcess(handler); }

}

customElements.define('agl-editor-ace', AglEditorAceWebComponent);
