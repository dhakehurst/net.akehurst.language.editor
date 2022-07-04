const webpack = require('webpack'); //to access built-in plugins
const path = require('path');
const HtmlWebPackPlugin = require('html-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader');

const isDevelopment = process.env.NODE_ENV !== 'production';

module.exports = {
    entry: {
        bundle: './src/index.js'
    },
    devtool: 'source-map',
    module: {
        rules: [
            { test: /\.vue$/, use: ['vue-loader'] },
            { test: /\.css$/, use: ['vue-style-loader', 'style-loader', 'css-loader'] },
            {
                test: /\.html$/,
                use: [  { loader: 'html-loader', options: {minimize: !isDevelopment} } ]
            }
        ],
    },
    resolve: {
        extensions: ['.js'],
        modules: ['node_modules'],
        alias: {
          'vue$': 'vue/dist/vue.esm.js'
        }
    },
    plugins: [
        new VueLoaderPlugin(),
        new HtmlWebPackPlugin({
            template: './src/index.html',
            filename: './index.html'
        })
    ]
};