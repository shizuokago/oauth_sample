<?php
require_once('./google-api-php-client/src/Google/autoload.php');

session_start();
$client = $_SESSION['client'];

if (isset($_GET['code'])) {
    $client->authenticate($_GET['code']);
    $_SESSION['token'] = $client->getAccessToken();
    header('Location: http://'.$_SERVER['HTTP_HOST'].'/callback');
    exit;
}

if (isset($_SESSION['token'])) {
    $client->setAccessToken($_SESSION['token']);
}

if ($client->getAccessToken()) {
    try {
        echo "Google Drive Api 連携完了！<br>";
        $_SESSION['client'] = $client;
    } catch (Google_Exception $e) {
        echo $e->getMessage();
    }
}

exit;

