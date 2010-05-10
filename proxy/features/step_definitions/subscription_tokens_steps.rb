require 'spec/expectations'
require 'candlepin_api'

Before do
    @subscription = {
        'subscription' => {'startDate' => '2007-07-13T00:00:00-04:00',
                           'endDate'   => '2010-07-13T00:00:00-04:00',
                           'quantity'  =>  37,
                           'productId' => 'provisioning'
        }
    }
    @token_name = nil
end

Then /^I have at least (\d+) subscription token[s]?$/ do |token_size|
    tokens = @candlepin.get_subscription_tokens()
    tokens.length.should >= token_size.to_i

    # XXX tokens are backwards for standalone right now, refering to existing
    # subscriptions which causes schema issues on owner delete. so just
    # manually remove them here.
    Given "there is no subscription token called \"#{@token_name}\"" 
end

Given /^I have a subscription token called "([^\"]*)"$/ do |token_name|
    @token_name = token_name
    token_id = get_token_id(token_name)
    if not token_id
        result = @candlepin.create_subscription(@test_owner['id'],
                                                @subscription)
        token = {
            'subscriptionToken' => {'token' => token_name,
                                    'subscription' => result['subscription'] }}
        @candlepin.create_subscription_token(token)
    end
end

Given /^there is no subscription token called "([^\"]*)"$/ do |token_name|
    token_id = get_token_id(token_name)
    if token_id
        @candlepin.delete_subscription_token(token_id)
    end
end

Then /^I can create a subscription token "([^\"]*)"$/ do |token_name|
    @token_name = token_name
    result = @candlepin.create_subscription(@test_owner['id'], @subscription)
    token = {'subscriptionToken' => {'token' => token_name,
                                     'subscription' => result['subscription'] }}
    @candlepin.create_subscription_token(token)

    Given "there is no subscription token called \"#{@token_name}\"" 
end

Then /^I can delete a subscription token "([^\"]*)"$/ do |token_name|
    @candlepin.delete_subscription_token(get_token_id(token_name))
    @token_name = nil
end

def get_token_id(token_name)
    tokens = @candlepin.get_subscription_tokens()
    matches = tokens.find_all{|token|
        token['subscriptionToken']['token'] == token_name}

    token_id = nil
    token_id = matches[0]['subscriptionToken']['id'] if matches.length > 0

    return token_id
end
