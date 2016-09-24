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

        $_SESSION['client'] = $client;

$service = new Google_Service_Gmail($client);

$user = 'me';
$results = $service->users_labels->listUsersLabels($user);

if (count($results->getLabels()) == 0) {
    print "No labels found.\n";
} else {
    print "Labels:\n";
    foreach ($results->getLabels() as $label) {
        printf("- %s\n", $label->getName());
    }
}

