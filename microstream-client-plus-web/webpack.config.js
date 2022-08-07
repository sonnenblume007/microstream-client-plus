const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');

module.exports = (env) => {
    return {
        entry: {
            scripts: path.resolve(__dirname, './src/main/js/app.js'),
        },
        output: {
            filename: '[name].js',
            path: path.resolve(__dirname, 'target/classes/ch/wipfli/microstreamclientplus/web/public/'),
            publicPath: '/public/'
        },
        mode: env.production
            ? 'production'
            : 'development',
        devtool: "eval-source-map",
        module: {
            rules: [
                {
                    test: /\.(js)$/,
                    exclude: /node_modules/,
                    use: ['babel-loader']
                },
                {
                    test: /\.css$/,
                    use: ['style-loader', 'css-loader']
                },
                {
                    test: /\.(s([ac])ss)$/,
                    use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader']
                },
                {
                    test: /\.(woff|woff2|eot|svg|ttf|jpg|png)$/,
                    type: 'asset/inline'
                }
            ]
        },
        resolve: {
            extensions: ['*', '.js']
        },
        plugins: [
            new MiniCssExtractPlugin(),
            new CopyWebpackPlugin
            ({
                patterns: [
                    {from: 'src/main/assets/images', to: 'assets/images'},
                ],
            }),
            new MonacoWebpackPlugin({
                languages: ['java']
            })
        ],
    };
};
