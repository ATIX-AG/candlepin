Feature: Virtualization guest and host recognition
    As a consumer
    I want to correctly consume entitlements as a virtual guest

    Scenario: A guest cannot consume virtual host entitlements
        Given I am a Consumer "guest_consumer" of type "virt_system"
        Then attempting to Consume an entitlement for the "72093906" product is forbidden
        And I Have 0 Entitlements
