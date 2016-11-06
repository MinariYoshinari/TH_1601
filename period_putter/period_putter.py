# -*- coding: utf-8 -*-

#機械学習関連
from sklearn.datasets import load_svmlight_file
from sklearn.cross_validation import train_test_split
from sklearn.grid_search import GridSearchCV
from sklearn.metrics import classification_report
from sklearn.svm import SVC
from sklearn.externals import joblib
import numpy as np

#形態素解析APIの使用
import urllib.request
import json

class Period_putter():

	def __init__(self):
		self.clf = joblib.load('clf3.pkl')
		self.all_pos_list = ['名詞', '名詞接尾辞', '冠名詞', '英語接尾辞', '動詞語幹', '動詞活用語尾', '動詞接尾辞', '冠動詞', '補助名詞', '形容詞語幹', '形容詞接尾辞', '冠形容詞', '連体詞', '連用詞', '接続詞', '独立詞', '接続接尾辞', '判定詞', '格助詞', '引用助詞', '連用助詞', '終助詞', '間投詞', '括弧', '句点', '読点', 'Symbol', 'Year', 'Month', 'Day', 'YearMonth', 'MonthDay', 'Hour', 'Minute', 'Second', 'HourMinute', 'MinuteSecond', 'PreHour', 'PostHour', 'Number', '助数詞', '助助数詞', '冠数詞', 'Alphabet', 'Kana', 'Katakana', 'Kanji', 'Roman', 'Undef']
		self.pos_list = []

	#ベクトルを2値ベクトルに変換
	def to_binary_vec(self, vec_dec):
		vec = []
		for (i,item) in enumerate(vec_dec):
			for j in range(50):
				if j == 49:
					vec.append(float(int(item == -1)))
				else:
					vec.append(float(int(item == self.all_pos_list[j])))
		return vec


	#形態素解析の結果から分類器用のベクトルをつくる
	def make_vec(self, result_json):
		#print(result_json)
		sentences = json.loads(result_json)["word_list"]
		pos_list = []

		#句点のない文章だからここでは必ず1文になるはずだけど，一応文分割処理入れておく
		for sentence in sentences:
			for pos in sentence:
				pos_list.append(pos[1])
		n = len(pos_list)
		pos_list.append('Undef')
		#句点の入る位置と思われる場所を基準として[2つ前，1つ前，基準, 1つ後，2つ後]
		vec_dec = [-1, -1, -1, -1]
		vecs = []
		for i in range(-1, n):
			vec_dec[0] = vec_dec[1]
			if i > -1:
				vec_dec[1] = pos_list[i] if vec_dec[2] == -1 else vec_dec[2]
			if i <= 0:
				vec_dec[2] = pos_list[i+1]
			elif i > n-2 or vec_dec[3] == -1:
				vec_dec[2] = -1
			else:
				vec_dec[2] = vec_dec[3]
			vec_dec[3] = pos_list[i+2] if i < n-2 else -1
			vecs.append(self.to_binary_vec(vec_dec))
			self.pos_list = pos_list
		return vecs


	#形態素解析APIにPOSTを投げる
	def post(self, contents):
		url = "https://labs.goo.ne.jp/api/morph"
		data = {"app_id":"93a5bf3d466870df366b5e860277fcf930695424168936452c31dfff0b8218a0", "sentence":contents, "info_filter":"form|pos"}
		json_data = json.dumps(data).encode("utf-8")
		method = "POST"
		headers = {"Content-Type" : "application/json"}

		# httpリクエストを準備してPOST
		request = urllib.request.Request(url, data=json_data, method=method, headers=headers)
		with urllib.request.urlopen(request) as response:
			response_body = response.read().decode("utf-8")
			return response_body
		return None

	#SVMで分類
	def classify(self,vecs):
		classes = self.clf.predict(np.array(vecs))
		return list(classes)

	def put(self, result, classes):
		sentences = json.loads(result)["word_list"]
		form_list = []

		#句点のない文章だからここでは必ず1文になるはずだけど，一応文分割処理入れておく
		for sentence in sentences:
			for pos in sentence:
				form_list.append(pos[0])

		ret_string = ""
		len_form = len(form_list)
		for i, c in enumerate(classes):
			if i != 0 and (c == 1.0 or self.pos_list[i-1] == "終助詞" or self.pos_list[i] == "undef"):
				ret_string += "。"
			if i < len_form:
				ret_string += form_list[i]
				if self.pos_list[i] in ["接続詞", "独立詞"]:
					ret_string += "、"
		return ret_string



	#ピリオド挿入プロセス全体
	def put_period(self, plane_string):
		result_json = self.post(plane_string)
		vecs_binary = self.make_vec(result_json)
		classification_results = self.classify(vecs_binary)
		string_with_period = self.put(result_json, classification_results)

		return string_with_period

# pp = Period_putter()
# #現段階ではインフルエンザが抜けると区切れなくなる
# print(pp.put_period("入れるよお大事にね"))
# print(pp.put_period("こんにちは君は可愛いねけれども凶暴だ"))
# #長い台詞のテスト用
# print(pp.put_period("寿くんの言ってる事は一つも分かんないよ寿くんがいいって言ってるもの何がいいのか分かんないよ分かんない私には分かんないのブラッティって何がカッコいいの血なんてイヤだよ痛いだけだよ黒のどこがカッコいいのクレイジーのどこがいいのか分かんない罪深いってなんなの罪があるののなのがいいの犯罪者がカッコいいのそもそも混沌てなにカオスだからなんなの闇ってなに暗ければいいの正義と悪だとなんで悪がいいの何で悪いほうがいいの悪いから悪じゃないの右腕がうずくと何でカッコいいの自分の力が制御できない感じがたまらないって何それただの間抜けな人じゃんちゃんと制御できるほうがカッコいいよ立派だよ普段は力を隠していると何が凄いのそんなのタダの手抜きだよ"))


import sys
pp = Period_putter()
print(pp.put_period(sys.argv[1]))
