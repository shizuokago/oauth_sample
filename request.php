<?php
require_once('./google-api-php-client/src/Google/autoload.php');

session_start();
$client = new Google_Client();
$client->setClientId($_POST["cid"]);
$client->setClientSecret($_POST["csecret"]);
$client->setRedirectUri('http://php.kneetenzero.appspot.com/callback');

$client->setScopes("https://www.googleapis.com/auth/gmail.readonly");
$_SESSION['client'] = $client;

$authUrl = $client->createAuthUrl();
header('Location: ' . $authUrl);
exit;
