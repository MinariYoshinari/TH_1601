# coding: utf-8
class CallbackController < ApplicationController
  def generate_message_content(user)
    token = RegistrationToken.generate_token_for(user)
    url = url_for controller: 'register', action: 'show', :id => token
    return [{
              type: 'text',
              text: "以下のURLにアクセスして登録作業をしてください。\n#{url}"
            }, {
              type: 'text',
              text: "あなたの友だちには以下のURLを送ってください。"
            }, {
              type: 'text',
              text:  user.friend_url
            }]

  end

  def callback
    body = request.body.string
    signature = request.env['HTTP_X_LINE_SIGNATURE']
    unless line_client.validate_signature(body, signature)
      logger.info 'Bad request'
      return head :ok
    end

    events = line_client.parse_events_from(body)
    logger.info body
    events.each do |event|
      case event
      when Line::Bot::Event::Message
        # メッセージが届いた時の実験的挙動
        case event.type
        when Line::Bot::Event::MessageType::Text
          user = User.find_by(user_id: event['source']['userId'])
          if event['source']['type'] == 'user' then
            line_client.reply_message(event['replyToken'], generate_message_content(user))
          end
        when Line::Bot::Event::MessageType::Image,
             Line::Bot::Event::MessageType::Video
          response = line_client.get_message_content(event.message['id'])
          tf = Tempfile.open('content')
          tf.write(response.body)
        end
      when Line::Bot::Event::Follow
        # ユーザー一覧に追加
        user_id = event['source']['userId']
        response = line_client.get_profile user_id
        json = JSON.parse(response.body)
        user = User.new
        user.user_id = user_id
        user.display_name = json['displayName']
        user.picture_url = json['pictureUrl']
        user.status_message = json['statusMessage']
        user.friend_token = User.generate_random_string
        user.save
        line_client.reply_message(event['replyToken'], generate_message_content(user))
      when Line::Bot::Event::Unfollow
        # ユーザー一覧から消去
        user_id = event['source']['userId']
        user = User.find_by user_id: user_id
        user.destroy
      when Line::Bot::Event::Join
      # ルーム、グループ一覧に追加
        case event['source']['type']
        when 'group'
          group_id = event['source']['groupId']
          group = Group.find_or_create_by group_id: group_id
        when 'room'
          room_id = event['source']['roomId']
          room = Room.find_or_create_by room_id: room_id
        end
      when Line::Bot::Event::Leave
        # ルーム、グループ一覧から消去
        case event['source']['type']
        when 'group'
          group_id = event['source']['groupId']
          group = Group.find_by group_id: group_id
          group.destroy
        when 'room'
          room_id = event['source']['roomId']
          room = Room.find_by room_id: room_id
          room.destroy
        end
      end
    end
    head :ok
  end
end
