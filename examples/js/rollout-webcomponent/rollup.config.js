import css from "rollup-plugin-import-css";
import commonjs from '@rollup/plugin-commonjs';

export default {
    input: "src/index.js",
    output: { file: "dist/bundle.js", format: "iief" },
    plugins: [
        css(),
        commonjs()
    ]
};