import {EditorView} from "codemirror"
import {Decoration, keymap, highlightSpecialChars, drawSelection, highlightActiveLine, dropCursor, rectangularSelection, crosshairCursor, lineNumbers, highlightActiveLineGutter, placeholder} from "@codemirror/view"
import {EditorState, StateEffect, StateField, Compartment} from "@codemirror/state"
import {defaultHighlightStyle, syntaxHighlighting, indentOnInput, bracketMatching, foldGutter, foldKeymap} from "@codemirror/language"
import {defaultKeymap, history, historyKeymap, indentWithTab} from "@codemirror/commands"
import {searchKeymap, highlightSelectionMatches} from "@codemirror/search"
import {autocompletion, completionKeymap, closeBrackets, closeBracketsKeymap} from "@codemirror/autocomplete"
import {lintKeymap, linter} from "@codemirror/lint"

// setup basic editor
let ev = new EditorView({
    doc: "",
    extensions: [
        lineNumbers(),
        highlightActiveLineGutter(),
        history(),
        foldGutter(),
        drawSelection(),
        dropCursor(),
        EditorState.allowMultipleSelections.of(true),
        bracketMatching(),
        autocompletion({activateOnTyping: false}),
        rectangularSelection(),
        crosshairCursor(),
        highlightActiveLine(),
        highlightSelectionMatches(),
        keymap.of([
            ...defaultKeymap,
            ...searchKeymap,
            ...historyKeymap,
            indentWithTab
        ]),
        placeholder('xyz')
    ],
    parent: document.body
});

// --- language support stuff
let _needsRefresh = true;           // indicates if text has changed
let tokensByLine = new Map(); // store token-style info

// detect text changes
function textChanged() {
    _needsRefresh = true;
}
let updateListener = EditorView.updateListener.of(v => {
    if(!v.docChanged || !v.viewportChanged) {
        // do nothing
    } else {
        textChanged()
    }
})


function receiveTokens(startLine, tokens) {
    console.log('receiveTokens '+startLine+' '+tokens.length)
    for (let i = 0; i < tokens.length; i++) {
        console.log('receiveTokens line '+(startLine+i)+' '+tokens[i].length)
        tokensByLine[startLine+i] = tokens[i];
    }
    updateVisibleTokens(ev)
}

function updateVisibleTokens(v) {
    console.log("updateVisibleTokens")
    let tokenEffects = [];
    for (let rng of v.visibleRanges) {
        let fromLine = v.state.doc.lineAt(rng.from).number
        let toLine = v.state.doc.lineAt(rng.to).number
        console.log('  visible '+fromLine+'-'+toLine)
        for (let ln = fromLine; ln <= toLine; ln++) {
            let lnToks = tokensByLine[ln-1];
            if(lnToks) {
                tokenEffects.push(tokenEffect.of(lnToks))
                for (let t of lnToks) {
                    console.log('    {'+t.style+' '+t.start+'-'+t.end+'}')
                }
            }
        }
    }
}

let themeCompartment = new Compartment();
let tokenEffect = StateEffect.define([])
let tokenUpdateListener = EditorView.updateListener.of(v => {
    if(v.docChanged || v.viewportChanged) {
        updateVisibleTokens(v.view)
    }
});

let decorationUpdater = StateField.define({
    create: (s)=>{ return Decoration.none },
    update: (ds,tr) => {
        console.log('decorationUpdate')
        let drs = ds.map(tr.changes);
        let marks = [];
        for (let e of tr.effects) {
            if(e.is(tokenEffect)) {
                console.log('  is tokenEffect')
                let lnToks = e.value
                for (let tk of lnToks) {
                    let m = Decoration.mark({
                        inclusiveEnd: true,
                        class: tk.style
                    }).range(tk.start, tk.end);
                    marks.push(m);
                }
            }
        }
        if(marks.length == 0) {
            return drs;
        } else {
            for (let m of marks) {
                console.log('    {'+m.class+' '+m.start+'-'+m.end+'}')
            }
            return drs.update({add:marks, filter:()=>{false}});
        }
    },
    provide: f => { return EditorView.decorations.from(f) },

});

let _linterPromise = null;
const RE = new RegExp('[a-zA-Z0-9_]+', 'g');
function processLine(line)  {
    let matches = line.matchAll(RE);
    let toks = []
    let p = 0;
    for(const mch of matches) {
        const st = mch.index
        const en = st+mch[0].length-1
        if(st > p) {
            // add tok for unmatched
            toks.push({
                start:p,
                end:st-1,
                style: 'nostyle',
            })
        }
        toks.push({
            start:st,
            end:en,
            style:'keyword',
        })
        p = en+1
    }
    if(p < line.length) {
        // add tok for unmatched after all matches
        toks.push({
            start:p,
            end:line.length-1,
            style: 'nostyle',
        })
    }
    return toks
}
function processText(text) {
    console.log('processText')
    let lines = text.split('\n')
    for (let i = 0; i < lines.length; i++) {
        let ln = lines[i]
        let lnToks = processLine(ln)
        receiveTokens(i, [lnToks])
    }
}
function lintSource(view) {
    console.log('lintSource')
    processText(view.state.doc.toString());

    // simulate processing done in worker and returned after a delay
    setTimeout(()=>{
        _linterPromise.resolve([])
        _linterPromise = null
    },300)

    return new Promise((resolve,reject) => {
        _linterPromise = {
            resolve: resolve,
            reject: reject,
        }
    });
}


// --- extend editor to add support for language processing
ev.dispatch({
    effects: [
        StateEffect.appendConfig.of([
            updateListener,
            // theme and token colors
            themeCompartment.of(EditorView.theme({})),
            tokenUpdateListener,
            decorationUpdater,
            linter(lintSource, {
                needsRefresh:(x)=>{
                    let r = _needsRefresh;
                    _needsRefresh = false;
                    r
                }
            })
        ])
    ]
});


// --- later set/update theme colors
ev.dispatch({
    effects: [
        themeCompartment.reconfigure(EditorView.theme({
            '.keyword': {
                'color': 'blue',
                'font-weight': 'bold'
            }
        })),
    ]
});
