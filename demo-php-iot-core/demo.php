<?php

require('vendor/autoload.php');

use WebSocket\Client;

declare(ticks=1);
if (!function_exists('pcntl_signal')) {
    printf("Error, you need to enable the pcntl extension in your php binary, see http://www.php.net/manual/en/pcntl.installation.php for more info%s", PHP_EOL);
    exit(1);
}
else {
    pcntl_signal(SIGINT, "sig_handler");
    pcntl_signal(SIGTERM, "sig_handler");
    pcntl_signal(SIGHUP,  "sig_handler");
}

//Check Args
if(count($argv) < 3) {
    printf("Usage: php demo.php <Device Id> <Auth Token>\n");
    exit(1);
}

$GLOBALS['url'] = "https://core.loka.systems";
$GLOBALS['device_id'] = $argv[1];
$token = $argv[2];
$GLOBALS['auth_token'] = "Bearer $token";


/** Subscribe Device */
$curl = curl_init("{$GLOBALS['url']}/subscribe_terminal/{$GLOBALS['device_id']}");
curl_setopt_array($curl, array(
    CURLOPT_RETURNTRANSFER => 1,
    CURLOPT_HTTPHEADER => array("Authorization: {$GLOBALS['auth_token']}")
));

$result = curl_exec($curl);
$output = json_decode($result, true);
if($result === false || ($output['status'] > 200)) {
    curl_close($curl);
    exit("Could not subscribe device\n");
}

curl_close($curl);
echo "Device {$GLOBALS['device_id']} subscribed\n";

$sec_context = array(
        'verify_peer' => false,
        'verify_peer_name' => false
);

$auth_header = array( 'Authorization' => $GLOBALS['auth_token'] );

$loop = React\EventLoop\Factory::create();
$connector = new Ratchet\Client\Connector($loop, null, $sec_context);
$connector(str_replace("http","ws", $GLOBALS['url']) . "/messages", ["RFC6455"], $auth_header)
    ->then(function(Ratchet\Client\WebSocket $conn) {
        echo "Waiting for messages...\n";
        $conn->on('message', function(\Ratchet\RFC6455\Messaging\MessageInterface $msg) use ($conn) {
            echo "Received: {$msg}\n";
        });

        $conn->on('close', function($code = null, $reason = null) {
            echo "Connection closed ({$code} - {$reason})\n";
            unsubscribe_device();
        });

    }, function(\Exception $e) use ($loop) {
        echo "$e\n";
        echo "Could not connect: {$e->getMessage()}\n";
        unsubscribe_device();
        $loop->stop();
    });

$loop->run();


function sig_handler($signo) {
    switch ($signo) {
        case SIGTERM:
            unsubscribe_device();
            exit;
            break;
        case SIGHUP:
            unsubscribe_device();
            exit;
            break;
        default:
            unsubscribe_device();
            exit;
            break;
    }
}

function unsubscribe_device() {
    echo "Unsubscribing device {$GLOBALS['device_id']}...\n";
    $curl = curl_init("{$GLOBALS['url']}/unsubscribe_terminal/{$GLOBALS['device_id']}");
    curl_setopt_array($curl, array(
        CURLOPT_RETURNTRANSFER => 1,
        CURLOPT_HTTPHEADER => array("Authorization: {$GLOBALS['auth_token']}")
    ));
    $result = curl_exec($curl);
    echo "End\n";
}

?>