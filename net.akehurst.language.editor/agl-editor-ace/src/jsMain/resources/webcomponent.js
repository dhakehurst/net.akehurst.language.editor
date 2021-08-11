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

    //Auto create properties for attributes
    if (this.constructor.observedAttributes && this.constructor.observedAttributes.length) {
      for(const attribute of this.constructor.observedAttributes) {
        Object.defineProperty(this, attribute, {
          get() { return this.getAttribute(attribute); },
          set(attrValue) {
            if (attrValue) {
              this.setAttribute(attribute, attrValue);
            } else {
              this.removeAttribute(attribute);
            }
          }
        });
      }
    }

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
    const editor = new AglEditorAce(element, this.languageId, this.editorId, options, this.workerScript);
  }

  attributeChangedCallback(attrName, oldValue, newValue) {
    if (newValue !== oldValue) {
      this[attrName] = newValue;
    }
  }

}

customElements.define('agl-editor-ace', AglEditorAceWebComponent);
