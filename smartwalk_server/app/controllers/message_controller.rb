require 'shellwords'

class MessageController < ApplicationController
  def push
    body = request.body.read
    json = JSON.parse(body)
    sender_mid = json['sender']
    display_name = json['display_name']
    receiver = json['receiver']
    sender_user = User.find_by(mid: sender_mid)
    unless receiver then
      begin
        user = User.select_first_friend_of(sender_mid, display_name)
        receiver = user.user_id
      rescue
        logger.debug("Cannot determine receiver")
        return head :bad_request
      end
    end

    msg_str = json['message']
    logger.debug("before process: " + msg_str)
    Dir.chdir("../period_putter") do
      msg_str = `python3 period_putter.py #{Shellwords.escape(msg_str)}`
    end
    logger.debug("after process: " + msg_str)
    message = {
      type: 'text',
      text: "#{sender_user.display_name}: #{msg_str}"
    }
    logger.debug("processed: " + message.to_s)

    logger.debug("Start pushing message...")
    response = line_client.push_message(receiver, message)
    logger.debug(response.code)
    logger.debug(response.body)

    head :ok
  end

  def can_push()
    body = request.body.read
    json = JSON.parse(body)
    sender_mid = json['sender']
    display_name = json['display_name']
    count = User.count_friends_of(sender_mid, display_name)
    logger.debug("count: " + count.to_s)
    result = { "can_push" => count == 1 }
    render json: result
  end
end
