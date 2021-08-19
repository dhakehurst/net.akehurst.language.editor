const path = require('path');
const HtmlWebPackPlugin = require('html-webpack-plugin');

const isDevelopment = process.env.NODE_ENV !== 'production';

module.exports = {
    entry: {
        bundle: './src/index.js',
        worker: './src/worker.js'
    },
    devtool: 'source-map',
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.html$/,
                use: [
                    {
                        loader: 'html-loader',
                        options: {minimize: !isDevelopment}
                    }
                ]
            }
        ],
    },
    resolve: {
        extensions: ['.js'],
        modules: ['node_modules']
    },
    plugins: [
        new HtmlWebPackPlugin({
            template: './src/index.html',
            filename: './index.html'
        })
    ]
};