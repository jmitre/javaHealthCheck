/**
 * Created by localadmin on 7/21/16.
 */
//package io.advantageous.examples;

import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.service.discovery.EndpointDefinition;
import io.advantageous.qbit.service.discovery.ServiceDefinition;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServiceDiscoveryMain {


    public static void main(String... args) throws Exception {

        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder =
                ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder();


        /* Attach to agent 2. */
        final ServiceDiscovery clientAgent2 = consulServiceDiscoveryBuilder.setConsulPort(7500).build();
        /* Attach to agent 1. */
        final ServiceDiscovery clientAgent1 = consulServiceDiscoveryBuilder.setConsulPort(8500).build();

        /* Start up service discovery. */
        clientAgent2.start();
        clientAgent1.start();

        /* Check in the services using their ids. */
        clientAgent1.checkInOk("myservice1");
        clientAgent2.checkInOk("myservice2");

        final ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {

            /* Let checks them in occasionally, and get a list of healthy nodes. */
            for (int index = 0; index < 100; index++) {
                System.out.println("Checking in....");

                /* Check in myservice1. */
                clientAgent1.checkInOk("myservice1");


                /* Check in myservice2. */
                clientAgent2.checkInOk("myservice2");
                Sys.sleep(100);


                /* Get a list of only the healthy nodes. */
                List<EndpointDefinition> serviceDefinitions = clientAgent1.loadServices("myservice");

                serviceDefinitions.forEach(serviceDefinition
                        -> System.out.println("FROM client agent 1: " + serviceDefinition));

                serviceDefinitions = clientAgent2.loadServices("myservice");

                serviceDefinitions.forEach(serviceDefinition
                        -> System.out.println("FROM client agent 2: " + serviceDefinition));

                Sys.sleep(10_000);

            }

            /* After 100 * 10 seconds stop. */
            clientAgent2.stop();
            clientAgent1.stop();
        });



    }

}
