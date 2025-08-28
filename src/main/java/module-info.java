module me.qblmchmmd.roming {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires kotlinx.serialization.core;
    requires kotlinx.serialization.json;
    requires kotlinx.coroutines.core;
    requires io.ktor.client.java;
    requires ch.qos.logback.classic;
    requires java.net.http;
    requires kotlinx.io.core;
    requires org.jetbrains.annotations;
    requires kotlinx.coroutines.javafx;


    opens me.qblmchmmd.roming to javafx.fxml;
    exports me.qblmchmmd.roming;
}